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
public interface IBeanContainer<BEAN extends IBean<BEAN>> extends IEncapsulatedHolder<IBeanContainerEncapsulated<BEAN>>
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
    getEncapsulated().addBean(Objects.requireNonNull(pBean), pIndex);
    BeanListenerUtil.beanAdded(this, pBean);
  }

  /**
   * Adds multiple beans to this container.
   *
   * @param pBeans iterable beans
   */
  default void addMultiple(Iterable<BEAN> pBeans)
  {
    addMultiple(StreamSupport.stream(pBeans.spliterator(), false));
  }

  /**
   * Adds multiple beans to this container.
   *
   * @param pBeanStream a stream of beans
   */
  default void addMultiple(Stream<BEAN> pBeanStream)
  {
    pBeanStream.forEach(this::addBean);
  }

  /**
   * Merges another container with this container.
   *
   * @param pOtherContainer the container to merge
   */
  default void merge(IBeanContainer<? extends BEAN> pOtherContainer)
  {
    //noinspection unchecked
    pOtherContainer.stream()
        .forEach(this::addBean);
  }

  /**
   * Replaces a bean at a certain index.
   * The registered listeners will be informed.
   *
   * @param pBean  the bean to replace
   * @param pIndex the index of the replacement
   * @return the replaced bean
   * @throws IndexOutOfBoundsException if, the index is not within the range of the contained beans
   */
  default BEAN replaceBean(BEAN pBean, int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);

    assert getEncapsulated() != null;
    BEAN removed = getEncapsulated().replaceBean(Objects.requireNonNull(pBean), pIndex);
    if (removed != null)
      BeanListenerUtil.beanRemoved(this, removed);
    BeanListenerUtil.beanAdded(this, pBean);
    return removed;
  }

  /**
   * Removes a bean (first occurrence) from the container.
   * The registered listeners will be informed.
   *
   * @param pBean the bean to remove
   * @return <tt>true</tt>, if the bean was removed successfully
   */
  default boolean removeBean(BEAN pBean)
  {
    Objects.requireNonNull(pBean);
    return BeanListenerUtil.removeFromContainer(this, pEncapsulated -> pEncapsulated.removeBean(pBean) ? pBean : null) != null;
  }

  /**
   * Removes a bean from the container by index.
   *
   * @param pIndex the index to remove the bean from
   * @return the removed bean
   * @throws IndexOutOfBoundsException if, the index is not within the range of the contained beans
   */
  default BEAN removeBean(int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);

    return BeanListenerUtil.removeFromContainer(this, pEncapsulated -> pEncapsulated.removeBean(pIndex));
  }

  /**
   * Removes all beans, that apply to a given predicate successfully.
   * The registered listeners will be informed.
   *
   * @param pPredicate the predicate to determine which beans should be removed
   * @return <tt>true</tt>, if at least one bean has been removed
   */
  default boolean removeBeanIf(Predicate<BEAN> pPredicate)
  {
    return BeanListenerUtil.removeBeanIf(this, pPredicate, false);
  }

  /**
   * Removes one bean which applies to a given predicate successfully and then stops the iteration.
   * This method should be used, if exactly one bean should be removed.
   * The registered listeners will be informed.
   *
   * @param pPredicate the predicate to determine which bean should be removed
   * @return <tt>true</tt>, if a bean has been removed
   */
  default boolean removeBeanIfAndBreak(Predicate<BEAN> pPredicate)
  {
    return BeanListenerUtil.removeBeanIf(this, pPredicate, true);
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
   * @param pField the bean field to retrieve the distinct values of
   * @param <TYPE> the field's data type
   * @return a Set with distinct values of this field
   */
  default <TYPE> Set<TYPE> getDistinctValuesFromField(IField<TYPE> pField)
  {
    return getDistinctValues(pBean -> pBean.getValue(Objects.requireNonNull(pField)));
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
    private static final String ERROR = "This container is read-only! The content can not be modified!";
    private final IBeanContainer<BEAN> originalContainer;

    public ReadOnly(IBeanContainer<BEAN> pOriginalContainer)
    {
      originalContainer = pOriginalContainer;
    }

    @Override
    public IBeanContainerEncapsulated<BEAN> getEncapsulated()
    {
      return originalContainer.getEncapsulated();
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
    public BEAN removeBean(int pIndex)
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public boolean removeBeanIf(Predicate<BEAN> pPredicate) throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public boolean removeBeanIfAndBreak(Predicate<BEAN> pPredicate)
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public void clear() throws UnsupportedOperationException
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public IBeanContainer<BEAN> withLimit(int pMaxCount, boolean pEvicting)
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public void sort(Comparator<BEAN> pComparator)
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public void merge(IBeanContainer<? extends BEAN> pOtherContainer)
    {
      throw new UnsupportedOperationException(ERROR);
    }

    @Override
    public List<BEAN> asList()
    {
      return Collections.unmodifiableList(originalContainer.asList());
    }
  }
}
