package de.adito.beans.core;

import de.adito.beans.core.annotations.internal.*;
import de.adito.beans.core.fields.IField;
import de.adito.beans.core.reactive.events.*;
import de.adito.beans.core.references.*;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.ReadOnlyInvocationHandler;
import io.reactivex.Observable;
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
 * This interface is implemented by the default bean container type {@link BeanContainer}.
 * Via static methods of this interface it is possible to create container instances.
 * But it may also be used for any other class that should be treated as a bean container.
 * Furthermore you are able to extend this interface through special methods for your use case.
 * Through the use of an interface it is possible to extend the container type to a class that already extends another class.
 * This might seem like a solution to the not available multi inheritance in Java, but here only the base interface type
 * is transferred to the extending class.
 *
 * This interface is very similar to {@link List}.
 * There are two reasons why it is not used as an extension of this interface:
 * - Some methods of {@link List} are not necessary and would increase to complexity, which would be contrary to concept of the bean modell
 * - It would not be possible to use this interface for a class, that implements {@link Iterable} trough its abstract base class, for example
 * (A class cannot be iterable for multiple types - this would limit the ability to transform any type to a bean type {@link ITransformable})
 *
 * @param <BEAN> the type of beans in this container
 * @author Simon Danner, 23.08.2016
 */
@RequiresEncapsulatedAccess
public interface IBeanContainer<BEAN extends IBean<BEAN>> extends IObservableBeanValues<BEAN, IBeanContainerEncapsulated<BEAN>>,
    IReferenceProvider
{
  /**
   * Creates an empty bean container.
   *
   * @param pBeanType the type of the beans in the container
   * @param <BEAN>    the generic type of the beans in the container
   * @return an empty bean container
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> empty(Class<BEAN> pBeanType)
  {
    return new BeanContainer<>(pBeanType);
  }

  /**
   * Creates a bean container containing a single bean.
   *
   * @param pBean  the initial bean to add
   * @param <BEAN> the generic type of the beans in the container
   * @return a new bean container with one initial bean
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofSingleBean(BEAN pBean)
  {
    return ofVariableNotEmpty(pBean);
  }

  /**
   * Creates a bean container containing an initial amount of beans from a stream.
   * The stream must at least contain one bean to infer the bean type from.
   *
   * @param pBeans a stream of beans to add to the new container
   * @param <BEAN> the generic type of the beans in the container
   * @return a new bean container with multiple initial beans
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofStreamNotEmpty(Stream<BEAN> pBeans)
  {
    return ofIterableNotEmpty(pBeans.collect(Collectors.toList()));
  }

  /**
   * Creates a bean container containing an initial amount of beans from a stream.
   * The stream may be empty.
   *
   * @param pBeanType the type of the beans in the container
   * @param pBeans    a stream of beans to add to the new container
   * @param <BEAN>    the generic type of the beans in the container
   * @return a new bean container with multiple initial beans
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofStream(Class<BEAN> pBeanType, Stream<BEAN> pBeans)
  {
    final IBeanContainer<BEAN> container = empty(pBeanType);
    container.addMultiple(pBeans);
    return container;
  }

  /**
   * Creates a new bean container containing an initial amount of beans from a varargs parameter.
   * Has to be one bean at least to infer the bean type.
   *
   * @param pBeans the beans to add initially
   * @param <BEAN> the generic type of the beans in the container
   * @return a new bean container with multiple initial beans
   */
  @SafeVarargs
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofVariableNotEmpty(BEAN... pBeans)
  {
    if (pBeans.length == 0)
      throw new RuntimeException("Unable to infer bean type! Empty varargs argument not allowed here!");
    //noinspection unchecked
    final Class<BEAN> type = (Class<BEAN>) pBeans[0].getClass();
    return ofVariable(type, pBeans);
  }

  /**
   * Creates a new bean container containing an initial amount of beans from a varargs parameter.
   *
   * @param pBeanType the type of the beans in the container
   * @param pBeans    the beans to add initially
   * @param <BEAN>    the generic type of the beans in the container
   * @return a new bean container with multiple initial beans
   */
  @SafeVarargs
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofVariable(Class<BEAN> pBeanType, BEAN... pBeans)
  {
    return ofIterable(pBeanType, Arrays.asList(pBeans));
  }

  /**
   * Creates a bean container containing an initial amount of beans from an iterable.
   * The iterable must at least contain one bean to infer the bean type from.
   *
   * @param pBeans an iterable of beans to add to the container initially
   * @param <BEAN> the generic type of the beans in the container
   * @return a new bean container with multiple initial beans
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofIterableNotEmpty(Iterable<BEAN> pBeans)
  {
    final Iterator<BEAN> it = pBeans.iterator();
    if (!it.hasNext())
      throw new RuntimeException("Unable to infer bean type! The amount of beans cannot be empty!");
    //noinspection unchecked
    final Class<BEAN> type = (Class<BEAN>) it.next().getClass();
    return ofIterable(type, pBeans);
  }

  /**
   * Creates a bean container containing an initial amount of beans from an iterable.
   * The iterable may be empty.
   *
   * @param pBeanType the type of the beans in the container
   * @param pBeans    an iterable of beans to add to the container initially
   * @param <BEAN>    the generic type of the beans in the container
   * @return a new bean container with multiple initial beans
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> ofIterable(Class<BEAN> pBeanType, Iterable<BEAN> pBeans)
  {
    return new BeanContainer<>(pBeanType, pBeans);
  }

  /**
   * Creates a new bean container with a custom encapsulated data core builder.
   * The custom data core may be used for another level of abstraction.
   *
   * @param pBeanType the type of the beans in the container
   * @param pBuilder  the custom data core builder
   * @param <BEAN>    the generic bean type of the container
   * @return an empty bean container with a custom data core
   * @see EncapsulatedBuilder
   */
  static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> withCustomEncapsulated(Class<BEAN> pBeanType,
                                                                                EncapsulatedBuilder.IContainerEncapsulatedBuilder<BEAN> pBuilder)
  {
    return new BeanContainer<>(pBeanType, pBuilder);
  }

  /**
   * The type of the beans in this container.
   *
   * @return a bean type
   */
  default Class<BEAN> getBeanType()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getBeanType();
  }

  /**
   * Adds a bean to this container.
   *
   * @param pBean the bean to add
   */
  @WriteOperation
  default void addBean(BEAN pBean)
  {
    addBean(pBean, size());
  }

  /**
   * Adds a bean at a certain index.
   *
   * @param pBean  the bean to add
   * @param pIndex the index to add the bean
   */
  @WriteOperation
  default void addBean(BEAN pBean, int pIndex)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addBean(Objects.requireNonNull(pBean), pIndex);
    BeanEvents.beanAdded(this, pBean);
  }

  /**
   * Adds multiple beans to this container.
   *
   * @param pBeans iterable beans
   */
  @WriteOperation
  default void addMultiple(Iterable<BEAN> pBeans)
  {
    addMultiple(StreamSupport.stream(pBeans.spliterator(), false));
  }

  /**
   * Adds multiple beans to this container.
   *
   * @param pBeanStream a stream of beans
   */
  @WriteOperation
  default void addMultiple(Stream<BEAN> pBeanStream)
  {
    pBeanStream.forEach(this::addBean);
  }

  /**
   * Merges this container with another container.
   *
   * @param pOtherContainer the container to merge
   */
  @WriteOperation
  default void merge(IBeanContainer<? extends BEAN> pOtherContainer)
  {
    pOtherContainer.stream()
        .forEach(this::addBean);
  }

  /**
   * Replaces a bean at a certain index.
   *
   * @param pBean  the bean to replace
   * @param pIndex the index of the replacement
   * @return the replaced bean
   * @throws IndexOutOfBoundsException if, the index is not within the range of the contained beans
   */
  @WriteOperation
  default BEAN replaceBean(BEAN pBean, int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);

    assert getEncapsulated() != null;
    BEAN removed = getEncapsulated().replaceBean(Objects.requireNonNull(pBean), pIndex);
    if (removed != null)
      BeanEvents.beanRemoved(this, removed);
    BeanEvents.beanAdded(this, pBean);
    return removed;
  }

  /**
   * Removes a bean (first occurrence) from the container.
   *
   * @param pBean the bean to remove
   * @return <tt>true</tt>, if the bean was removed successfully
   */
  @WriteOperation
  default boolean removeBean(BEAN pBean)
  {
    Objects.requireNonNull(pBean);
    return BeanEvents.removeFromContainer(this, pEncapsulated -> pEncapsulated.removeBean(pBean) ? pBean : null) != null;
  }

  /**
   * Removes a bean from the container by index.
   *
   * @param pIndex the index to remove the bean from
   * @return the removed bean
   * @throws IndexOutOfBoundsException if, the index is not within the range of the contained beans
   */
  @WriteOperation
  default BEAN removeBean(int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);

    return BeanEvents.removeFromContainer(this, pEncapsulated -> pEncapsulated.removeBean(pIndex));
  }

  /**
   * Removes all beans, that apply to a given predicate successfully.
   * The registered listeners will be informed.
   *
   * @param pPredicate the predicate to determine which beans should be removed
   * @return <tt>true</tt>, if at least one bean has been removed
   */
  @WriteOperation
  default boolean removeBeanIf(Predicate<BEAN> pPredicate)
  {
    return BeanEvents.removeBeanIf(this, pPredicate, false);
  }

  /**
   * Removes one bean which applies to a given predicate successfully and then stops the iteration.
   * This method should be used, if exactly one bean should be removed.
   * The registered listeners will be informed.
   *
   * @param pPredicate the predicate to determine which bean should be removed
   * @return <tt>true</tt>, if a bean has been removed
   */
  @WriteOperation
  default boolean removeBeanIfAndBreak(Predicate<BEAN> pPredicate)
  {
    return BeanEvents.removeBeanIf(this, pPredicate, true);
  }

  /**
   * Returns the bean at a certain index.
   *
   * @param pIndex the index
   * @return the bean at the given index
   */
  default BEAN getBean(int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);

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
    return getEncapsulated().indexOfBean(Objects.requireNonNull(pBean));
  }

  /**
   * The amount of beans in this container.
   *
   * @return the number of beans
   */
  default int size()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().size();
  }

  /**
   * Determines, if this container is empty.
   *
   * @return <tt>true</tt>, if the container contains no beans
   */
  default boolean isEmpty()
  {
    return size() == 0;
  }

  /**
   * Clears this container.
   */
  @WriteOperation
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
    assert getEncapsulated() != null;
    return getEncapsulated().indexOfBean(Objects.requireNonNull(pBean)) >= 0;
  }

  /**
   * Sorts this bean container according to a given comparator.
   *
   * @param pComparator the comparator
   */
  @WriteOperation
  default void sort(Comparator<BEAN> pComparator)
  {
    assert getEncapsulated() != null;
    getEncapsulated().sort(pComparator);
  }

  /**
   * Sets a limit (= number of beans) for this container.
   * If the number of beans exceeds the limit, beans will be removed from the beginning of this container until the limit is reached.
   *
   * @param pMaxCount the limit (-1 for no limit)
   * @param pEvicting <tt>true</tt>, if the first beans should be removed, when the limit is reached
   */
  @WriteOperation
  default IBeanContainer<BEAN> withLimit(int pMaxCount, boolean pEvicting)
  {
    assert getEncapsulated() != null;
    getEncapsulated().setLimit(pMaxCount, pEvicting);
    return this;
  }

  /**
   * Creates an {@link Observable} to observe bean addition events.
   *
   * @return an observable that publishes {@link BeanContainerAddition} events
   */
  default Observable<BeanContainerAddition<BEAN>> observeAdditions()
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().observeByType(BeanContainerAddition.class)
        .map(pChange -> (BeanContainerAddition<BEAN>) pChange);
  }

  /**
   * Creates an {@link Observable} to observe bean removal events.
   *
   * @return an observable that publishes {@link BeanContainerRemoval} events
   */
  default Observable<BeanContainerRemoval<BEAN>> observeRemovals()
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().observeByType(BeanContainerRemoval.class)
        .map(pChange -> (BeanContainerRemoval<BEAN>) pChange);
  }

  /**
   * The statistic data of this container.
   * This data contains the number of beans in this container at several points of time.
   * This data may not be available, if there's no annotation set. In this case this method will return null.
   *
   * @return the statistic data of this container, or null if not present
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
   * @param pField  the bean field to retrieve the distinct values of
   * @param <VALUE> the field's data type
   * @return a Set with distinct values of this field
   */
  default <VALUE> Set<VALUE> getDistinctValuesFromField(IField<VALUE> pField)
  {
    return getDistinctValues(pBean -> pBean.getValue(Objects.requireNonNull(pField)));
  }

  /**
   * Evaluates all distinct values of beans that are determined by a certain value-resolverType.
   * The resolverType will be applied to all beans of the container and the unique resulting values will be returned as a Set.
   * Null-values are not collected.
   *
   * @param pValueResolver the resolverType to retrieve a value from a bean
   * @param <VALUE>        the generic data type of the values
   * @return a Set containing all distinct values based on the resolverType
   */
  default <VALUE> Set<VALUE> getDistinctValues(Function<BEAN, VALUE> pValueResolver)
  {
    return stream()
        .map(pValueResolver)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  /**
   * This bean container as {@link List}.
   * This can be used as proxy, where it is necessary to provide the bean container as list.
   *
   * @return this container as List interface
   */
  default List<BEAN> asList()
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
    //noinspection unchecked
    return ReadOnlyInvocationHandler.createReadOnlyInstance(IBeanContainer.class, this);
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

  @Override
  default Set<BeanReference> getDirectReferences()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getDirectReferences();
  }

  /**
   * A list proxy for a bean container to treat it as a {@link java.util.List}.
   *
   * @param <BEAN> the type of the beans in the container
   */
  final class BeanContainerListProxy<BEAN extends IBean<BEAN>> extends AbstractList<BEAN>
  {
    private final IBeanContainer<BEAN> container;

    /**
     * Creates the list proxy based on an original bean container.
     *
     * @param pContainer the original bean container
     */
    private BeanContainerListProxy(IBeanContainer<BEAN> pContainer)
    {
      container = pContainer;
    }

    @Override
    public BEAN get(int pIndex)
    {
      return container.getBean(pIndex);
    }

    @Override
    public int size()
    {
      return container.size();
    }

    @Override
    public BEAN set(int pIndex, BEAN pBean)
    {
      return container.replaceBean(pBean, pIndex);
    }

    @Override
    public void add(int pIndex, BEAN pBean)
    {
      container.addBean(pBean, pIndex);
    }

    @Override
    public BEAN remove(int pIndex)
    {
      return container.removeBean(pIndex);
    }
  }
}
