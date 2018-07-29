package de.adito.beans.core;

import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.listener.*;
import de.adito.beans.core.mappers.IBeanFlatDataMapper;
import de.adito.beans.core.statistics.*;
import de.adito.beans.core.util.*;
import de.adito.beans.core.util.exceptions.BeanFieldDoesNotExistException;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.*;

/**
 * A factory to create the encapsulated data cores for the bean elements.
 * A data core will be created based on builders, which provide the least required functionality to define the core.
 * The builder may hold the data itself or describe how the data can be retrieved (e.g. database).
 *
 * The builder interfaces can be found here as well:
 * - {@link IBeanEncapsulatedBuilder}
 * - {@link IContainerEncapsulatedBuilder}
 *
 * Default builders for the basic beans or bean containers can be found here:
 * - {@link de.adito.beans.core.Bean.DefaultEncapsulatedBuilder}
 * - {@link de.adito.beans.core.BeanContainer.DefaultEncapsulatedBuilder}
 *
 * The concept of using builders to create the data cores brings the advantage to define custom data cores for the bean elements.
 * In this way it's possible to use external data sources directly as encapsulated data core (abstraction).
 * The custom data cores can be injected through methods of this class.
 * If you want to create a special bean or bean container type with custom data cores, there are constructors in the base classes,
 * which expect a builder based on which the cores will be created in this case.
 *
 * @author Simon Danner, 11.02.2018
 */
public final class EncapsulatedBuilder
{
  private EncapsulatedBuilder()
  {
  }

  /**
   * Injects the default encapsulated data core into a bean.
   *
   * @param pBean  the bean to inject the default core into
   * @param <BEAN> the generic bean type
   * @return the bean
   */
  public static <BEAN extends IBean<BEAN>> BEAN injectDefaultEncapsulated(BEAN pBean)
  {
    return injectCustomEncapsulated(pBean, new Bean.DefaultEncapsulatedBuilder(pBean));
  }

  /**
   * Injects the default encapsulated data core into a bean container
   *
   * @param pContainer the container to inject the default core into
   * @param <BEAN>     the generic type of the beans in the container
   * @return the container
   */
  public static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> injectDefaultEncapsulated(IBeanContainer<BEAN> pContainer)
  {
    return injectCustomEncapsulated(pContainer, new BeanContainer.DefaultEncapsulatedBuilder<>(pContainer.asList()));
  }

  /**
   * Injects a custom encapsulated data cora into a bean.
   * The data core is based on a {@link IBeanEncapsulatedBuilder}
   *
   * @param pBean          the bean to inject the custom core into
   * @param pCustomBuilder the builder to create the core
   * @param <BEAN>         the generic bean type
   * @return the bean
   */
  public static <BEAN extends IBean<BEAN>> BEAN injectCustomEncapsulated(BEAN pBean, IBeanEncapsulatedBuilder pCustomBuilder)
  {
    if (!Bean.class.isAssignableFrom(pBean.getClass()))
      throw new RuntimeException(pBean + " has to be a regular bean (based on the bean base class).");
    //noinspection unchecked
    ((Bean<BEAN>) pBean).setEncapsulated(pCustomBuilder);
    return pBean;
  }

  /**
   * Injects a custom encapsulated data cora into a bean container.
   * The data core is based on a {@link IContainerEncapsulatedBuilder}
   *
   * @param pContainer     the bean container to inject the custom core into
   * @param pCustomBuilder the builder to create the core
   * @param <BEAN>         the type of the beans in the container
   * @return the bean container
   */
  public static <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> injectCustomEncapsulated(IBeanContainer<BEAN> pContainer,
                                                                                         IContainerEncapsulatedBuilder<BEAN> pCustomBuilder)
  {
    if (!BeanContainer.class.isAssignableFrom(pContainer.getClass()))
      throw new RuntimeException(pContainer + " has to be a regular bean container (based on the bean container base class).");
    //noinspection unchecked
    ((BeanContainer<BEAN>) pContainer).setEncapsulated(pCustomBuilder);
    return pContainer;
  }

  /**
   * Creates a bean encapsulated data core based on a {@link IBeanEncapsulatedBuilder}.
   *
   * @param pBuilder  the builder (may hold data)
   * @param pBeanType the type of the bean to core is for
   * @param <BEAN>    the generic bean type
   * @return the newly created encapsulated data core
   */
  static <BEAN extends IBean<BEAN>> IBeanEncapsulated<BEAN> createBeanEncapsulated(IBeanEncapsulatedBuilder pBuilder, Class<BEAN> pBeanType)
  {
    return new _BeanEncapsulated<>(pBuilder, pBeanType, null);
  }

  /**
   * Creates a bean encapsulated data core based on a {@link IBeanEncapsulatedBuilder}.
   *
   * @param pBuilder    the builder (may hold data)
   * @param pBeanType   the type of the bean to core is for
   * @param pBeanFields a list of fields for the bean type
   * @param <BEAN>      the generic bean type
   * @return the newly created encapsulated data core
   */
  static <BEAN extends IBean<BEAN>> IBeanEncapsulated<BEAN> createBeanEncapsulated(IBeanEncapsulatedBuilder pBuilder,
                                                                                   Class<BEAN> pBeanType, List<IField<?>> pBeanFields)
  {
    return new _BeanEncapsulated<>(pBuilder, pBeanType, pBeanFields);
  }

  /**
   * Creates a encapsulated data core for a bean container based on a {@link IContainerEncapsulatedBuilder}.
   *
   * @param pBuilder  the builder (may hold data)
   * @param pBeanType the type of the beans in the container
   * @param <BEAN>    the generic bean type
   * @return the newly created encapsulated data core
   */
  static <BEAN extends IBean<BEAN>> IBeanContainerEncapsulated<BEAN> createContainerEncapsulated(IContainerEncapsulatedBuilder<BEAN> pBuilder,
                                                                                                 Class<BEAN> pBeanType)
  {
    return new _ContainerEncapsulated<>(pBuilder, pBeanType);
  }

  /**
   * The builder interface for the encapsulated bean data cores.
   * Defines the least required functionality to define the data core.
   */
  public interface IBeanEncapsulatedBuilder extends Iterable<FieldTuple<?>>
  {
    /**
     * The value for a bean field.
     *
     * @param pField the bean field
     * @param <TYPE> the data type of the field
     * @return the value for the field
     */
    <TYPE> TYPE getValue(IField<TYPE> pField);

    /**
     * Sets a value for a bean field.
     *
     * @param pField         the bean field
     * @param pValue         the new value
     * @param pAllowNewField <tt>true</tt>, if a new field should be created, if it isn't existing
     * @param <TYPE>         the data type of the field
     */
    <TYPE> void setValue(IField<TYPE> pField, TYPE pValue, boolean pAllowNewField);

    /**
     * Removes a bean field.
     *
     * @param pField the bean field to remove
     * @param <TYPE> the data type of the field
     */
    <TYPE> void removeField(IField<TYPE> pField);
  }

  /**
   * The builder interface for the encapsulated bean container data cores.
   * Defines the least required functionality to define the data core.
   */
  public interface IContainerEncapsulatedBuilder<BEAN extends IBean<BEAN>> extends Iterable<BEAN>
  {
    /**
     * Adds a bean at a certain index.
     *
     * @param pBean  the bean to add
     * @param pIndex the index
     */
    void addBean(BEAN pBean, int pIndex);

    /**
     * Removes the first occurrence of a certain bean.
     *
     * @param pBean the bean to remove
     * @return <tt>true</tt>, if the bean has been removed successfully
     */
    boolean removeBean(BEAN pBean);

    /**
     * Removes a bean by index.
     *
     * @param pIndex the index to remove
     * @return the removed bean
     */
    BEAN removeBean(int pIndex);

    /**
     * Gets a bean by its index.
     *
     * @param pIndex the index of the bean
     * @return the bean at the certain index
     */
    BEAN getBean(int pIndex);

    /**
     * Returns the index of a certain bean within the core.
     * -1, if the bean is not present within the core.
     *
     * @param pBean the bean
     * @return the index of the bean
     */
    int indexOfBean(BEAN pBean);

    /**
     * The amount of beans.
     */
    int size();

    /**
     * Sorts this bean container according to a given comparator.
     *
     * @param pComparator the comparator
     */
    void sort(Comparator<BEAN> pComparator);
  }

  /**
   * The bean encapsulated implementation based on the builder.
   *
   * @param <BEAN> the type of the bean the core is for
   */
  private static class _BeanEncapsulated<BEAN extends IBean<BEAN>> implements IBeanEncapsulated<BEAN>
  {
    private final IBeanEncapsulatedBuilder builder;
    private final List<IField<?>> fieldOrder;
    private Map<IField<?>, IStatisticData> statisticData;
    private final BeanEncapsulatedContainers<BEAN, IBeanChangeListener<BEAN>> containers = new BeanEncapsulatedContainers<>();

    private _BeanEncapsulated(IBeanEncapsulatedBuilder pBuilder, Class<BEAN> pBeanType, @Nullable List<IField<?>> pFields)
    {
      builder = pBuilder;
      fieldOrder = new ArrayList<>(pFields == null ? BeanReflector.reflectBeanFields(pBeanType) : pFields);
      _createStatisticData(pBeanType);
    }

    @Override
    public <TYPE> TYPE getValue(IField<TYPE> pField)
    {
      return _ifFieldExistsWithResult(pField, builder::getValue);
    }

    @Override
    public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue)
    {
      _ifFieldExists(pField, pCheckedField -> builder.setValue(pCheckedField, pValue, false));
    }

    @Override
    public <TYPE> void addField(IField<TYPE> pField, int pIndex)
    {
      builder.setValue(pField, null, true);
      fieldOrder.add(pIndex, pField);
    }

    @Override
    public <TYPE> void removeField(IField<TYPE> pField)
    {
      _ifFieldExists(pField, pCheckedField -> {
        builder.removeField(pCheckedField);
        fieldOrder.remove(pCheckedField);
      });
    }

    @Override
    public int getFieldCount()
    {
      return isFieldFiltered() ? (int) streamFields().count() : fieldOrder.size();
    }

    @Override
    public Map<IField<?>, IStatisticData> getStatisticData()
    {
      return Collections.unmodifiableMap(statisticData);
    }

    @Override
    public Stream<IField<?>> streamFields()
    {
      assert getContainers() != null;
      return fieldOrder.stream()
          .filter(pField -> containers.getFieldFilters().stream()
              .allMatch(pFieldPredicate -> pFieldPredicate.test(pField, builder.getValue(pField))));
    }

    @Override
    public BeanEncapsulatedContainers<BEAN, IBeanChangeListener<BEAN>> getContainers()
    {
      return containers;
    }

    @NotNull
    @Override
    public Iterator<FieldTuple<?>> iterator()
    {
      return _createFilteredTupleStream().iterator();
    }

    /**
     * Creates the statistic data for this encapsulated core.
     * This data contains a set of entries with the value of a field for a certain timestamp.
     * It may contain multiple sets for every field annotated with {@link Statistics}.
     *
     * @param pBeanType the type of the bean
     */
    private void _createStatisticData(Class<? extends IBean> pBeanType)
    {
      statisticData = BeanReflector.getBeanStatisticAnnotations(pBeanType).entrySet().stream()
          .collect(Collectors.toMap(pEntry -> BeanUtil.findFieldByName(fieldOrder.stream(), pEntry.getKey()), pEntry ->
          {
            Statistics statistics = pEntry.getValue();
            return new StatisticData<>(statistics.capacity(), null);
          }));
    }

    /**
     * Creates a stream of all tuples of this data core, that are currently active.
     * Some may be filtered at a certain moment.
     *
     * @return a stream of field tuples
     */
    private Stream<FieldTuple<?>> _createFilteredTupleStream()
    {
      return _applyMappings()
          .filter(pTuple -> containers.getFieldFilters().stream()
              .allMatch(pPredicate -> pPredicate.test(pTuple.getField(), pTuple.getValue())));
    }

    /**
     * Applies all data mappers to the data stream of the builder.
     * Effectively every data mapper leads to a flatMap()-operation
     *
     * @return a stream of field tuples
     */
    private Stream<FieldTuple<?>> _applyMappings()
    {
      Stream<FieldTuple<?>> stream = StreamSupport.stream(builder.spliterator(), false);
      for (BeanEncapsulatedContainers.BeanDataMapper dataMapper : containers.getDataMappers())
      {
        IBeanFlatDataMapper mapper = dataMapper.getDataMapper();
        final AtomicInteger changes = new AtomicInteger(); //Changes for each mapping iteration
        do
        {
          changes.set(0);
          stream = stream.flatMap(pTuple -> {
            IField<?> beanField = dataMapper.getBeanField();
            if ((beanField == null || beanField == pTuple.getField()) && mapper.affectsTuple(pTuple.getField(), pTuple.getValue()))
            {
              changes.getAndIncrement();
              return mapper.flatMapTuple(pTuple.getField(), pTuple.getValue());
            }
            else
              return Stream.of(pTuple);
          })
              .collect(Collectors.toList())
              .stream();
        }
        while (!mapper.isCompleted(changes.get()));
      }

      return stream;
    }

    /**
     * Checks, if a certain field is existing at a certain time.
     * Field filters are considered as well.
     * If the field is existing, a action (based on the field) will be performed and the produced result will be returned.
     *
     * @param pField   the field to check
     * @param pAction  the on the field based action to get the result from
     * @param <TYPE>   the field's data type
     * @param <RETURN> the result type
     * @return the result of the field based action
     */
    private <TYPE, RETURN> RETURN _ifFieldExistsWithResult(IField<TYPE> pField, Function<IField<TYPE>, RETURN> pAction)
    {
      if (!containsField(pField))
        throw new BeanFieldDoesNotExistException(pField);
      return pAction.apply(pField);
    }

    /**
     * Checks, if a certain field is existing at a certain time.
     * Field filters are considered as well.
     * If the field is existing, a action (based on the field) will be performed with no result
     *
     * @param pField  the field to check
     * @param pAction the on the field based action to perform
     * @param <TYPE>  the field's data type
     */
    private <TYPE> void _ifFieldExists(IField<TYPE> pField, Consumer<IField<TYPE>> pAction)
    {
      if (!containsField(pField))
        throw new BeanFieldDoesNotExistException(pField);
      pAction.accept(pField);
    }
  }

  /**
   * The bean container encapsulated implementation based on the builder.
   *
   * @param <BEAN> the type of the beans in the container
   */
  private static class _ContainerEncapsulated<BEAN extends IBean<BEAN>> implements IBeanContainerEncapsulated<BEAN>
  {
    private final IContainerEncapsulatedBuilder<BEAN> builder;
    private final Class<BEAN> beanType;
    private _LimitInfo limitInfo = null;
    private final IStatisticData<Integer> statisticData;
    private final EncapsulatedContainers<BEAN, IBeanContainerChangeListener<BEAN>> containers = new EncapsulatedContainers<>();

    public _ContainerEncapsulated(IContainerEncapsulatedBuilder<BEAN> pBuilder, Class<BEAN> pBeanType)
    {
      builder = pBuilder;
      beanType = pBeanType;
      statisticData = _createStatisticData();
    }

    @Override
    public Class<BEAN> getBeanType()
    {
      return beanType;
    }

    @Override
    public void addBean(BEAN pBean, int pIndex)
    {
      if (pIndex < 0 || pIndex > size())
        throw new IndexOutOfBoundsException("index: " + pIndex);

      //Is the limit reached?
      if (limitInfo != null && limitInfo.limit == size())
      {
        if (!limitInfo.evicting)
          throw new RuntimeException("The limit of this container is reached! limit: " + limitInfo.limit);
        removeBean(getBean(0)); //Remove first bean if limit is reached and evicting flag is set
        pIndex--;
      }

      builder.addBean(pBean, pIndex);
    }

    @Override
    public BEAN replaceBean(BEAN pReplacement, int pIndex)
    {
      if (pIndex < 0 || pIndex >= size())
        throw new IndexOutOfBoundsException("index: " + pIndex);

      BEAN removed = removeBean(pIndex);
      addBean(pReplacement, pIndex);
      return removed;
    }

    @Override
    public boolean removeBean(BEAN pBean)
    {
      return builder.removeBean(pBean);
    }

    @Override
    public BEAN removeBean(int pIndex)
    {
      if (pIndex < 0 || pIndex >= size())
        throw new IndexOutOfBoundsException("index: " + pIndex);

      return builder.removeBean(pIndex);
    }

    @Override
    public BEAN getBean(int pIndex)
    {
      return builder.getBean(pIndex);
    }

    @Override
    public int indexOfBean(BEAN pBean)
    {
      return builder.indexOfBean(pBean);
    }

    @Override
    public int size()
    {
      return builder.size();
    }

    @Override
    public void sort(Comparator<BEAN> pComparator)
    {
      builder.sort(pComparator);
    }

    @Override
    public void setLimit(int pMaxCount, boolean pEvicting)
    {
      if (pMaxCount >= 0)
      {
        int diffToMany = size() - pMaxCount;
        if (diffToMany > 0)
          IntStream.range(0, diffToMany)
              .forEach(pIndex -> removeBean(0));
      }
      limitInfo = pMaxCount < 0 ? null : new _LimitInfo(pMaxCount, pEvicting);
    }

    @Nullable
    @Override
    public IStatisticData<Integer> getStatisticData()
    {
      return statisticData;
    }

    @Override
    public EncapsulatedContainers<BEAN, IBeanContainerChangeListener<BEAN>> getContainers()
    {
      return containers;
    }

    @NotNull
    @Override
    public Iterator<BEAN> iterator()
    {
      return builder.iterator();
    }

    /**
     * Creates the statistic data for this encapsulated core.
     * This data is an amount of timestamps with an associated number,
     * which stands for the amount of beans in this container at the timestamp.
     *
     * @return the statistic data for this encapsulated core
     */
    @Nullable
    private IStatisticData<Integer> _createStatisticData()
    {
      Statistics statistics = beanType.getAnnotation(Statistics.class);
      return statistics != null ? new StatisticData<>(statistics.capacity(), size()) : null;
    }

    /**
     * Information about the limit of this container core.
     * Contains the limit itself and the information if old entries should be evicted.
     */
    private class _LimitInfo
    {
      private final int limit;
      private final boolean evicting;

      public _LimitInfo(int pLimit, boolean pEvicting)
      {
        limit = pLimit;
        evicting = pEvicting;
      }
    }
  }
}
