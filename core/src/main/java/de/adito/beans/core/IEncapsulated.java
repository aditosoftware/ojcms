package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.*;
import de.adito.beans.core.util.weak.IInputSortedElements;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * The encapsulated base core of a bean element.
 * Defines the private scope on a Java level related to the bean data.
 *
 * Important: This interface must be package protected to enable data encapsulation.
 *
 * @param <CORE>     the type of the elements in the core
 * @param <BEAN>     the generic bean type that uses this data core
 * @param <LISTENER> the type of the bean listeners that can be registered here
 * @author Simon Danner, 25.01.2017
 */
interface IEncapsulated<CORE, BEAN extends IBean<BEAN>, LISTENER extends IBeanChangeListener<BEAN>,
    CONTAINERS extends EncapsulatedContainers<BEAN, LISTENER>> extends Iterable<CORE>, ILinkable, IReferable
{
  /**
   * The additional information containers of this core. (Not the data itself)
   * This is the replacement for an abstract class.
   * All other methods have a default implementation that relate to this base data.
   *
   * @return additional information containers
   */
  CONTAINERS getContainers();

  /**
   * The container of all weak-registered listeners.
   */
  default IInputSortedElements<LISTENER> getWeakListeners()
  {
    assert getContainers() != null;
    return getContainers().getWeakListenerContainer();
  }

  /**
   * The container of all weak-registered linked transformable components to this core.
   */
  default Collection<ITransformable> getWeakLinkedContainer()
  {
    assert getContainers() != null;
    return getContainers().getWeakLinkContainer();
  }

  @Override
  default Map<IBean<?>, Set<IHierarchicalField<?>>> getWeakReferenceMap()
  {
    assert getContainers() != null;
    return getContainers().getWeakReferences();
  }

  /**
   * Registers a weak listener.
   *
   * @param pListener the listener
   */
  default void addListener(LISTENER pListener)
  {
    assert getWeakListeners() != null;
    synchronized (getWeakListeners())
    {
      if (!getWeakListeners().add(pListener))
        throw new RuntimeException("Duplicate listeners are not allowed for one bean element!");
    }
  }

  /**
   * Removes a listener.
   *
   * @param pListener the listener to remove
   */
  default void removeListener(LISTENER pListener)
  {
    assert getWeakListeners() != null;
    synchronized (getWeakListeners())
    {
      getWeakListeners().remove(pListener);
    }
  }

  /**
   * Fires an event to all registered listeners.
   *
   * @param pAction the action to perform
   */
  default void fire(Consumer<LISTENER> pAction)
  {
    assert getWeakListeners() != null;
    synchronized (getWeakListeners())
    {
      getWeakListeners().forEach(pAction);
    }
  }

  @Override
  default <LINK extends ITransformable> boolean isLinked(LINK pComponent)
  {
    assert getWeakLinkedContainer() != null;
    return getWeakLinkedContainer().contains(pComponent);
  }

  @Override
  default <LINK extends ITransformable> void registerWeakLink(LINK pComponent)
  {
    assert getWeakLinkedContainer() != null;
    getWeakLinkedContainer().add(pComponent);
  }

  /**
   * The hierarchical structure of references to this bean.
   *
   * @return a interface to retrieve information about the hierarchical reference structure
   */
  default IHierarchicalStructure getHierarchicalStructure()
  {
    return new HierarchicalStructureImpl<>(this);
  }

  /**
   * A stream of all core elements of this data core.
   */
  default Stream<CORE> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * A parallel stream of all core elements of this data core.
   */
  default Stream<CORE> parallelStream()
  {
    return StreamSupport.stream(spliterator(), true);
  }

  /**
   * Default implementation of the hierarchical reference structure.
   */
  class HierarchicalStructureImpl<C, B extends IBean<B>, L extends IBeanChangeListener<B>, CO extends EncapsulatedContainers<B, L>>
      implements IHierarchicalStructure
  {
    private final IEncapsulated<C, B, L, CO> encapsulated;

    public HierarchicalStructureImpl(IEncapsulated<C, B, L, CO> pEncapsulated)
    {
      encapsulated = pEncapsulated;
    }

    @Override
    public void destroy()
    {
      encapsulated.getWeakReferenceMap().clear();
    }

    @Override
    public Set<IHierarchicalNode> getDirectParents()
    {
      return encapsulated.getWeakReferenceMap().entrySet().stream()
          .flatMap(pEntry -> pEntry.getValue().stream()
              .map(pField -> new HierarchicalNodeImpl(pEntry.getKey(), pField)))
          .collect(Collectors.toSet());
    }
  }

  /**
   * A default implementation for a hierarchical node.
   */
  class HierarchicalNodeImpl implements IHierarchicalBeanStructure.IHierarchicalNode
  {
    private final IBean<?> bean;
    private final IHierarchicalField<?> field;

    public HierarchicalNodeImpl(IBean<?> pBean, IHierarchicalField<?> pField)
    {
      bean = pBean;
      field = pField;
    }

    @Override
    public IBean<?> getBean()
    {
      return bean;
    }

    @Override
    public IHierarchicalField<?> getField()
    {
      return field;
    }

    @Override
    public boolean equals(Object pObject)
    {
      if (this == pObject) return true;
      if (pObject == null || getClass() != pObject.getClass()) return false;

      HierarchicalNodeImpl other = (HierarchicalNodeImpl) pObject;
      return bean == other.bean && field == other.field;
    }

    @Override
    public int hashCode()
    {
      int result = bean.hashCode();
      result = 31 * result + field.hashCode();
      return result;
    }
  }
}
