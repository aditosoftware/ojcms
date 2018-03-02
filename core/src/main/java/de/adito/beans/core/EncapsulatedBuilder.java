package de.adito.beans.core;

import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.listener.*;
import de.adito.beans.core.statistics.*;
import de.adito.beans.core.util.*;
import org.jetbrains.annotations.*;

import java.util.*;
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
  public static <BEAN extends IBean<BEAN>> IBeanEncapsulated<BEAN> createBeanEncapsulated(IBeanEncapsulatedBuilder pBuilder, Class<BEAN> pBeanType)
  {
    return new _BeanEncapsulated<>(pBuilder, pBeanType);
  }

  /**
   * Creates a encapsulated data core for a bean container based on a {@link IContainerEncapsulatedBuilder}.
   *
   * @param pBuilder  the builder (may hold data)
   * @param pBeanType the type of the beans in the container
   * @param <BEAN>    the generic bean type
   * @return the newly created encapsulated data core
   */
  public static <BEAN extends IBean<BEAN>> IBeanContainerEncapsulated<BEAN> createContainerEncapsulated(IContainerEncapsulatedBuilder<BEAN> pBuilder,
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
     * Removes a bean.
     *
     * @param pBean the bean to remove
     * @return <tt>true</tt>, if the bean has been removed successfully
     */
    boolean removeBean(BEAN pBean);

    /**
     * Determines, if a certain bean is contained.
     *
     * @param pBean the bean to check
     * @return <tt>true</tt>, if the bean is contained
     */
    boolean containsBean(BEAN pBean);

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
    private final BeanBaseData<BEAN, IBeanChangeListener<BEAN>> baseData = new BeanBaseData<>();

    private _BeanEncapsulated(IBeanEncapsulatedBuilder pBuilder, Class<BEAN> pBeanType)
    {
      builder = pBuilder;
      fieldOrder = new ArrayList<>(BeanReflector.getBeanMetadata(pBeanType));
      _createStatisticData(pBeanType);
    }

    @Override
    public <TYPE> TYPE getValue(IField<TYPE> pField)
    {
      return builder.getValue(pField);
    }

    @Override
    public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue)
    {
      builder.setValue(pField, pValue, false);
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
      builder.removeField(pField);
      fieldOrder.remove(pField);
    }

    @Override
    public <TYPE> int getFieldIndex(IField<TYPE> pField)
    {
      if (!fieldOrder.contains(pField))
        throw new RuntimeException("The field " + pField.getName() + " is not present at the bean. Its index cannot be resolved.");
      return fieldOrder.indexOf(pField);
    }

    @Override
    public int getFieldCount()
    {
      return fieldOrder.size();
    }

    @Override
    public Map<IField<?>, IStatisticData> getStatisticData()
    {
      return Collections.unmodifiableMap(statisticData);
    }

    @Override
    public Stream<IField<?>> streamFields()
    {
      return fieldOrder.stream();
    }

    @Override
    public BeanBaseData<BEAN, IBeanChangeListener<BEAN>> getBeanBaseData()
    {
      return baseData;
    }

    @NotNull
    @Override
    public Iterator<FieldTuple<?>> iterator()
    {
      return builder.iterator();
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
            return new StatisticData<>(statistics.intervall(), statistics.capacity(), null);
          }));
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
    private final BeanBaseData<BEAN, IBeanContainerChangeListener<BEAN>> baseData = new BeanBaseData<>();

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
      //Is the limit reached?
      if (limitInfo != null && limitInfo.limit == size())
      {
        if (!limitInfo.evicting)
          throw new RuntimeException("The limit of this container is reached already! Limit: " + limitInfo.limit);
        removeBean(getBean(0)); //Remove first bean if limit is reached and evicting flag is set
      }

      builder.addBean(pBean, pIndex);
    }

    @Override
    public BEAN replaceBean(BEAN pReplacement, int pIndex)
    {
      BEAN toRemove = getBean(pIndex);
      if (toRemove != null)
        removeBean(toRemove);
      addBean(pReplacement, pIndex);
      return toRemove;
    }

    @Override
    public boolean removeBean(BEAN pBean)
    {
      return builder.removeBean(pBean);
    }

    @Override
    public boolean containsBean(BEAN pBean)
    {
      return builder.containsBean(pBean);
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
    public void setLimit(int pMaxCount, boolean pEvicting)
    {
      limitInfo = pMaxCount < 0 ? null : new _LimitInfo(pMaxCount, pEvicting);
    }

    @Nullable
    @Override
    public IStatisticData<Integer> getStatisticData()
    {
      return statisticData;
    }

    @Override
    public BeanBaseData<BEAN, IBeanContainerChangeListener<BEAN>> getBeanBaseData()
    {
      return baseData;
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
      Statistics statistics = BeanReflector.getContainerStatisticAnnotation(beanType);
      return statistics != null ? new StatisticData<>(statistics.intervall(), statistics.capacity(), size()) : null;
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
