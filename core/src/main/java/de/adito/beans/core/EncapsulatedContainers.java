package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.core.util.weak.*;

import java.util.*;

/**
 * A container for base data of a encapsulated data core.
 * It kind of replaces the necessity of an abstract class.
 *
 * @param <BEAN>     the type of the beans in the core
 * @param <LISTENER> the type of the bean listeners managed here
 * @author Simon Danner, 04.09.2017
 */
class EncapsulatedContainers<BEAN extends IBean<BEAN>, LISTENER extends IBeanChangeListener<BEAN>>
{
  private final IInputSortedElements<LISTENER> listenerContainer = new WeakInputSortedContainer<>();
  private final Set<ITransformable> linkContainer = Collections.newSetFromMap(new WeakHashMap<>());
  private final Map<IBean<?>, Set<IHierarchicalField<?>>> references = new WeakHashMap<>();

  /**
   * A container with weak-registered listeners.
   */
  public IInputSortedElements<LISTENER> getWeakListenerContainer()
  {
    return listenerContainer;
  }

  /**
   * A container to store links between transformed bean elements. (weak)
   */
  public Set<ITransformable> getWeakLinkContainer()
  {
    return linkContainer;
  }

  /**
   * A container that registers weak references to this bean core.
   * The reference is established via the bean (the wrapper of the core).
   */
  public Map<IBean<?>, Set<IHierarchicalField<?>>> getWeakReferences()
  {
    return references;
  }
}
