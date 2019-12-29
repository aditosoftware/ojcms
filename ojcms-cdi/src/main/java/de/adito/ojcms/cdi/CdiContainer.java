package de.adito.ojcms.cdi;

import de.adito.picoservice.IPicoRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.se.*;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Static entry point to boot the CDI container.
 * Also registers all {@link AbstractCustomContext} types and provides an injectable {@link ICdiControl}.
 *
 * @author Simon Danner, 27.12.2019
 */
public final class CdiContainer
{
  private static final Map<Class<? extends Annotation>, AbstractCustomContext> CUSTOM_CONTEXTS;

  static
  {
    CUSTOM_CONTEXTS = IPicoRegistry.INSTANCE.find(AbstractCustomContext.class, CustomCdiContext.class).keySet().stream()
        .map(CdiContainer::_createContext)
        .collect(Collectors.toMap(Context::getScope, identity()));
  }

  @Produces
  @ApplicationScoped
  private static ICdiControl CDI_CONTROL;

  /**
   * Boots the CDI container. After a successful startup process {@link ICdiControl} will be available via injection.
   * To reboot the CDI container {@link ICdiControl#shutdown()} has to be called first. Otherwise a subsequent call
   * will lead to a runtime exception.
   *
   * @return an interface to control and create CDI elements
   */
  public static ICdiControl boot()
  {
    if (CDI_CONTROL != null)
      throw new IllegalStateException("Cdi container already booted!");

    final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
    CDI_CONTROL = new _CdiControl(initializer.initialize());
    return CDI_CONTROL;
  }

  /**
   * Provides all custom scope annotation types.
   *
   * @return a set of scope annotation types
   */
  static Set<Class<? extends Annotation>> getAllCustomScopeAnnotationTypes()
  {
    return Collections.unmodifiableSet(CUSTOM_CONTEXTS.keySet());
  }

  /**
   * Provides all custom context instances.
   *
   * @return a set of custom context instances
   */
  static Set<AbstractCustomContext> getAllCustomContextInstances()
  {
    return new HashSet<>(CUSTOM_CONTEXTS.values());
  }

  /**
   * Initializes a custom context instance by calling the default constructor.
   *
   * @param pContextType the type of the context to initialize
   * @return the initialized context instance
   */
  private static <CONTEXT extends AbstractCustomContext> CONTEXT _createContext(Class<CONTEXT> pContextType)
  {
    try
    {
      return pContextType.newInstance();
    }
    catch (InstantiationException | IllegalAccessException pE)
    {
      throw new RuntimeException("A custom cdi context must define a public default constructor!", pE);
    }
  }

  /**
   * Implementation of {@link ICdiControl} based on {@link SeContainer}.
   */
  private static class _CdiControl implements ICdiControl
  {
    private final SeContainer container;

    private _CdiControl(SeContainer pContainer)
    {
      container = Objects.requireNonNull(pContainer);
    }

    @Override
    public <T> T createInjected(Class<T> pType)
    {
      return container.select(pType).get();
    }

    @Override
    public <T> T injectInstance(T pInstance)
    {
      final BeanManager beanManager = container.getBeanManager();
      //noinspection unchecked
      final Class<T> instanceType = (Class<T>) pInstance.getClass();
      final AnnotatedType<T> annotatedType = beanManager.createAnnotatedType(instanceType);
      final InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(annotatedType);

      injectionTarget.inject(pInstance, beanManager.createCreationalContext(null));
      return pInstance;
    }

    @Override
    public IActiveContext startContext(Class<? extends Annotation> pScopeAnnotationType)
    {
      return _retrieveFromContext(pScopeAnnotationType, AbstractCustomContext::startNewContext);
    }

    @Override
    public boolean isContextActive(Class<? extends Annotation> pScopeAnnotationType)
    {
      return _retrieveFromContext(pScopeAnnotationType, AbstractCustomContext::isActive);
    }

    @Override
    public void shutdown()
    {
      container.close();
      CDI_CONTROL = null;
    }

    /**
     * Retrieves some information or performs some action on a {@link AbstractCustomContext} instance.
     *
     * @param pScopeAnnotationType the custom scope's annotation type to identify the context instance
     * @param pRetriever           a function to retrieve any information from the context instance
     * @param <R>                  the type of the result
     * @return the result obtained from the context instance
     */
    private <R> R _retrieveFromContext(Class<? extends Annotation> pScopeAnnotationType, Function<AbstractCustomContext, R> pRetriever)
    {
      if (!CUSTOM_CONTEXTS.containsKey(pScopeAnnotationType))
        throw new RuntimeException("No suitable custom context found for scope annotation: " + pScopeAnnotationType.getName());

      final AbstractCustomContext context = CUSTOM_CONTEXTS.get(pScopeAnnotationType);
      return pRetriever.apply(context);
    }
  }
}
