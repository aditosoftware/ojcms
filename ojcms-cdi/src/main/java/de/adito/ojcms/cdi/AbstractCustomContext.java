package de.adito.ojcms.cdi;

import de.adito.picoservice.PicoService;

import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.*;
import java.util.*;

/**
 * Base class for a custom CDI context. These are used to define new scopes within the application.
 * The context class must define an annotation type that is later used the define the scope.
 *
 * These custom context types are detected automatically via {@link PicoService} and registered with the CDI container afterwards.
 * So to add any custom context you only have to define a concrete class extending this class providing the scope's annotation type
 * and being annotated by {@link CustomCdiContext}.
 *
 * Internal details: Each context type holds its own thread-local stack. When a new context should be started,
 * a new context element is put on the stack. Vice versa, when the context is finished, the element will be removed from the stack.
 * Through the thread-local concept each "call" has its own context stack. So every time the CDI container tries to create a new bean
 * for the associated context scope, it will be added to the context element on top of the stack.
 *
 * @author Simon Danner, 25.12.2019
 * @see NormalScope
 */
public abstract class AbstractCustomContext implements Context
{
  private final ThreadLocal<Stack<_ActiveContext>> CONTEXT_STACK = ThreadLocal.withInitial(Stack::new);

  /**
   * Starts a new context. Internally puts a new container for contextual instances on the context stack.
   *
   * @return a control element for the started context
   */
  public IActiveContext startNewContext()
  {
    return _stack().push(new _ActiveContext());
  }

  @Override
  public <T> T get(Contextual<T> pContextual, CreationalContext<T> pCreationalContext)
  {
    return _stack().peek().getOrCreate(pContextual, pCreationalContext);
  }

  @Override
  public <T> T get(Contextual<T> pContextual)
  {
    return _stack().peek().get(pContextual);
  }

  @Override
  public boolean isActive()
  {
    return !_stack().empty();
  }

  /**
   * The stack of currently active context elements (by thread).
   *
   * @return a stack of active context instances
   */
  private Stack<_ActiveContext> _stack()
  {
    return CONTEXT_STACK.get();
  }

  /**
   * Describes an active context element for the stack. It manages contextual instances for the associated scope
   * by receiving calls from the CDI container to retrieve beans from the cache or to create new instances.
   */
  private class _ActiveContext implements IActiveContext
  {
    private final Map<Contextual<?>, _ContextualInstance<?>> instances = new HashMap<>();
    private boolean destroyed;

    /**
     * Retrieves a contextual instance from the cache or creates a new one if not existing.
     *
     * @param pContextual        a contextual reference
     * @param pCreationalContext context for the creation process
     * @param <T>                the type of the instance to retrieve or create
     * @return the requested contextual instance
     */
    <T> T getOrCreate(Contextual<T> pContextual, CreationalContext<T> pCreationalContext)
    {
      //noinspection unchecked
      return (T) instances.computeIfAbsent(pContextual, pContext -> new _ContextualInstance<>(pCreationalContext, pContextual)).instance;
    }

    /**
     * Retrieves a contextual instance from the cache. This method will return 'null' if the instance has never been created.
     *
     * @param pContextual a contextual reference
     * @param <T>         the type of the instance to retrieve
     * @return the requested contextual instance or null if not created yet
     */
    <T> T get(Contextual<T> pContextual)
    {
      //noinspection unchecked
      return Optional.ofNullable(instances.get(pContextual))
          .map(pContextualInstance -> (T) pContextualInstance.instance)
          .orElse(null);
    }

    @Override
    public void destroy()
    {
      if (destroyed)
        throw new IllegalStateException("Context not active anymore!");

      instances.values().forEach(_ContextualInstance::destroy);
      _stack().pop();
      destroyed = true;
    }
  }

  /**
   * Holder for a contextual instance.
   *
   * @param <T> the generic type of the instance
   */
  private static class _ContextualInstance<T>
  {
    private final T instance;
    private final CreationalContext<T> creationalContext;
    private final Contextual<T> contextual;

    /**
     * Creates a new contextual instance via {@link Contextual#create(CreationalContext)} and stores the involved contextual references.
     *
     * @param pCreationalContext context for the creation process
     * @param pContextual        the contextual reference to create an instance for
     */
    private _ContextualInstance(CreationalContext<T> pCreationalContext, Contextual<T> pContextual)
    {
      instance = pContextual.create(pCreationalContext);
      creationalContext = pCreationalContext;
      contextual = pContextual;
    }

    /**
     * Destroys the contextual instance.
     */
    private void destroy()
    {
      contextual.destroy(instance, creationalContext);
    }
  }
}
