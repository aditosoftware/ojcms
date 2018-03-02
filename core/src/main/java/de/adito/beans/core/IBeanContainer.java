package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.BeanContainerListProxy;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * The functional wrapper interface of a bean container.
 * A bean container is separated in this wrapper and an encapsulated data core.
 * This interface provides the whole functionality via default methods.
 * The default methods use the only non-default method {@link IEncapsulatedHolder#getEncapsulated()} to get access to the data core.
 * This method may be called 'virtual field', because it gives access to an imaginary field that holds the data core.
 * This means you only have to give a reference to any bean container core to get a completed container, when this interface is used.
 *
 * This interface is implemented by the default bean container type {@link BeanContainer}, which is used to create containers for beans anywhere.
 * But it may also be used for any other class that should be treated as a bean container.
 * Furthermore you are able to extend this interface through special methods for your use case.
 * Through the use of an interface it is possible to extend the container type to a class that already extends another class.
 * This might seem like a solution to the not available multi inheritance in Java, but here only the base interface type
 * is transferred to the extending class.
 *
 * This interface is very similar to the Java List interface.
 * There are two reasons why it is not used as an extension of this interface:
 * - Some methods of {@link List} are not necessary and would increase to complexity, which would be contrary to concept of the bean modell
 * - It would not be possible to use this interface for a class, that implements {@link Iterable} trough its abstract base class, for example
 * (A class cannot be iterable for multiple types - this would limit the ability to transform any type to a bean type {@link ITransformable})
 *
 * @param <BEAN> the type of beans in this container
 * @author Simon Danner, 23.08.2016
 */
public interface IBeanContainer<BEAN extends IBean<BEAN>> extends IEncapsulatedHolder<IBeanContainerEncapsulated<BEAN>>
{
  /**
   * The type of the beans in this container.
   */
  default Class<BEAN> getBeanType()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getBeanType();
  }

  /**
   * Adds a bean to this container.
   * The registered listeners will be informed.
   *
   * @param pBean the bean to add
   */
  default void addBean(BEAN pBean)
  {
    addBean(pBean, size());
  }

  /**
   * Adds a bean at a certain index.
   * The registered listeners will be informed.
   *
   * @param pBean  the bean to add
   * @param pIndex the index to add the bean
   */
  default void addBean(BEAN pBean, int pIndex)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addBean(pBean, pIndex);
    BeanListenerUtil.beanAdded(this, pBean);
  }

  /**
   * Replaces a bean at a certain index.
   * The registered listeners will be informed.
   *
   * @param pBean  the bean to replace
   * @param pIndex the index of the replacement
   * @return the replaced bean
   */
  default BEAN replaceBean(BEAN pBean, int pIndex)
  {
    assert getEncapsulated() != null;
    BEAN removed = getEncapsulated().replaceBean(pBean, pIndex);
    if (removed != null)
      BeanListenerUtil.beanRemoved(this, removed);
    BeanListenerUtil.beanAdded(this, pBean);
    return removed;
  }

  /**
   * Removes a bean from the container.
   * The registered listeners will be informed.
   *
   * @param pBean the bean to remove
   * @return <tt>true</tt>, if the bean was removed successfully
   */
  default boolean removeBean(BEAN pBean)
  {
    IBeanContainerEncapsulated<BEAN> enc = getEncapsulated();
    assert enc != null;
    boolean removed = enc.removeBean(pBean);
    if (removed)
      BeanListenerUtil.beanRemoved(this, pBean);
    return removed;
  }

  /**
   * Removes all beans which apply to a given predicate successfully.
   * The registered listeners will be informed.
   *
   * @param pPredicate the predicate to determine which beans should be removed
   * @return <tt>true</tt>, if at least one bean has been removed
   */
  default boolean removeBeanIf(Predicate<BEAN> pPredicate)
  {
    assert getEncapsulated() != null;
    Iterator<BEAN> it = getEncapsulated().iterator();
    boolean removed = false;
    while (it.hasNext())
    {
      BEAN bean = it.next();
      if (pPredicate.test(bean))
      {
        it.remove();
        BeanListenerUtil.beanRemoved(this, bean);
        removed = true;
      }
    }
    return removed;
  }

  /**
   * Returns the bean at a certain index.
   *
   * @param pIndex the index
   * @return the bean at the given index
   */
  default BEAN getBean(int pIndex)
  {
    if (pIndex < 0)
      throw new RuntimeException("The index must be greater than 0. Given index: " + pIndex);

    assert getEncapsulated() != null;
    return getEncapsulated().getBean(pIndex);
  }

  /**
   * Returns the index of a certain bean.
   * -1, if the bean is not present within the container.
   *
   * @param pBean the bean to determine the index
   * @return the index of the bean within the container
   */
  default int indexOf(BEAN pBean)
  {
    assert getEncapsulated() != null;
    return getEncapsulated().indexOfBean(pBean);
  }

  /**
   * The amount of beans in this container.
   */
  default int size()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().size();
  }

  /**
   * Clears this container.
   */
  default void clear()
  {
    assert getEncapsulated() != null;
    removeBeanIf(pBean -> true); //Do not just simply clear the data core, else the listeners won't be informed
  }

  /**
   * Determines, if the container contains a certain bean.
   *
   * @param pBean the bean
   * @return <tt>true</tt>, if the bean is contained
   */
  default boolean contains(BEAN pBean)
  {
    if (pBean == null)
      throw new IllegalArgumentException("The bean must not be null!");
    assert getEncapsulated() != null;
    return getEncapsulated().containsBean(Objects.requireNonNull(pBean));
  }

  /**
   * Sets a limit (= number of beans) for this container.
   *
   * @param pMaxCount the limit (-1 for no limit)
   * @param pEvicting <tt>true</tt>, if the first beans should be removed, when the limit is reached
   */
  default IBeanContainer<BEAN> withLimit(int pMaxCount, boolean pEvicting)
  {
    assert getEncapsulated() != null;
    getEncapsulated().setLimit(pMaxCount, pEvicting);
    return this;
  }

  /**
   * Registers a weak change listener for this container.
   * It will be informed about additions and removals and also about the single changes of each bean in the container.
   *
   * @param pChangeListener the listener to register
   */
  default void listenWeak(IBeanContainerChangeListener<BEAN> pChangeListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addListener(pChangeListener);
  }

  /**
   * Unregisters a listener.
   *
   * @param pChangeListener the listener to unregister
   */
  default void unlisten(IBeanContainerChangeListener<BEAN> pChangeListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().removeListener(pChangeListener);
  }

  /**
   * The statistic data of this container.
   * This data contains the number of beans in this container at several points of time (based on an intervall).
   * This data may not be available, if there's no annotation set. In this case this method will return null.
   */
  @Nullable
  default IStatisticData<Integer> getStatisticData()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getStatisticData();
  }

  /**
   * Evaluates all distinct values based on a certain bean field.
   * In other words this method will take all values from the beans in this container associated with the field
   * and remove all duplicate values.
   *
   * @param pField the bean field to retrieve the distinct values of
   * @param <TYPE> the field's data type
   * @return a Set with distinct values of this field
   */
  default <TYPE> Set<TYPE> getDistinctValuesFromField(IField<TYPE> pField)
  {
    return getDistinctValues(pBean -> pBean.getValue(pField));
  }

  /**
   * Evaluates all distinct values of beans that are determined by a certain value-resolver.
   * The resolver will be applied to all beans of the container and the unique resulting values will be returned as a Set.
   * Null-values are not collected.
   *
   * @param pValueResolver the resolver to retrieve a value from a bean
   * @param <TYPE>         the generic data type of the values
   * @return a Set containing all distinct values based on the resolver
   */
  default <TYPE> Set<TYPE> getDistinctValues(Function<BEAN, TYPE> pValueResolver)
  {
    return stream()
        .map(pValueResolver)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toSet());
  }

  /**
   * Returns this container as List.
   * This can be used as proxy, where it is necessary to provide the bean container as List.
   *
   * @return this container as List interface
   */
  default List<BEAN> toListProxy()
  {
    return new BeanContainerListProxy<>(this);
  }

  /**
   * This container as read only version.
   * This will be a new instance, but the data core stays the same.
   *
   * @return this container as read only version
   */
  default IBeanContainer<BEAN> asReadOnly()
  {
    assert getEncapsulated() != null;
    return new ReadOnly<>(this);
  }

  /**
   * A stream of beans contained in this container.
   */
  default Stream<BEAN> stream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().stream();
  }

  /**
   * A parallel stream of beans contained in this container.
   */
  default Stream<BEAN> parallelStream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().parallelStream();
  }

  /**
   * Performs a given action for every bean in this container.
   *
   * @param pAction the action that should be performed for every bean
   */
  default void forEachBean(Consumer<BEAN> pAction)
  {
    stream().forEach(pAction);
  }

  /**
   * Default read-only container implementation.
   *
   * @param <BEAN> the type of beans in this container
   */
  class ReadOnly<BEAN extends IBean<BEAN>> implements IBeanContainer<BEAN>
  {
    private static final String ERROR = "Read-only container!";
    private final IBeanContainer<BEAN> original;

    public ReadOnly(IBeanContainer<BEAN> pOriginal)
    {
      original = pOriginal;
    }

    @Override
    public IBeanContainerEncapsulated<BEAN> getEncapsulated()
    {
      return original.getEncapsulated();
    }

    @Override
    public void addBean(BEAN pBean) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public void addBean(BEAN pBean, int pIndex) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public BEAN replaceBean(BEAN pBean, int pIndex) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public boolean removeBean(BEAN pBean) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public boolean removeBeanIf(Predicate<BEAN> pPredicate) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public void clear() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }
  }
}
