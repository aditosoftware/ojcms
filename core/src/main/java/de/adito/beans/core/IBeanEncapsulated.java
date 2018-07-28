package de.adito.beans.core;

import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.mappers.*;
import de.adito.beans.core.references.IHierarchicalBeanStructure;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.IBeanFieldPredicate;

import java.util.Map;
import java.util.stream.Stream;

import static de.adito.beans.core.BeanEncapsulatedContainers.BeanDataMapper;

/**
 * Defines the data core for a bean.
 * It contains a collection of field value tuples.
 *
 * @param <BEAN> the generic bean type that uses this data core
 * @author Simon Danner, 20.01.2017
 */
interface IBeanEncapsulated<BEAN extends IBean<BEAN>> extends IEncapsulated<FieldTuple<?>, BEAN, IBeanChangeListener<BEAN>,
    BeanEncapsulatedContainers<BEAN, IBeanChangeListener<BEAN>>>
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
   * @param pField the bean field
   * @param pValue the new value
   * @param <TYPE> the data type of the field
   */
  <TYPE> void setValue(IField<TYPE> pField, TYPE pValue);

  /**
   * Adds a bean field to the data core.
   *
   * @param pField the bean field to add
   * @param pIndex the index at which the field should be added (the index includes private fields)
   * @param <TYPE> the data type of the field
   */
  <TYPE> void addField(IField<TYPE> pField, int pIndex);

  /**
   * Removes a bean field from the data core.
   *
   * @param pField the bean field to remove
   * @param <TYPE> the data type of the field
   */
  <TYPE> void removeField(IField<TYPE> pField);

  /**
   * The amount of bean fields of this data core.
   *
   * @return the amount of fields (includes private fields)
   */
  int getFieldCount();

  /**
   * A map of the statistic data of this core.
   * The data is grouped by the bean fields.
   *
   * @return a map that provides statistic data for a bean field
   */
  Map<IField<?>, IStatisticData> getStatisticData();

  /**
   * A stream of bean fields of this data core.
   */
  Stream<IField<?>> streamFields();

  /**
   * Adds a field filter to this data core.
   * So fields with their associated values may be excluded for a certain time.
   *
   * @param pPredicate the predicate to define the excluded fields
   */
  default void addFieldFilter(IBeanFieldPredicate pPredicate)
  {
    assert getContainers() != null;
    getContainers().getFieldFilters().add(pPredicate);
  }

  /**
   * Removes a field filter from this data core.
   *
   * @param pPredicate the predicate/filter to remove
   */
  default void removeFieldFilter(IBeanFieldPredicate pPredicate)
  {
    assert getContainers() != null;
    getContainers().getFieldFilters().remove(pPredicate);
  }

  /**
   * Clears all field filters.
   */
  default void clearFieldFilters()
  {
    assert getContainers() != null;
    getContainers().getFieldFilters().clear();
  }

  /**
   * Determines, if there are any field filters set.
   *
   * @return <tt>true</tt>, if there is one filter at least
   */
  default boolean isFieldFiltered()
  {
    assert getContainers() != null;
    return !getContainers().getFieldFilters().isEmpty();
  }

  /**
   * Adds a temporary data mapper to this data core.
   *
   * @param pDataMapper the data mapper
   */
  default void addDataMapper(IBeanFlatDataMapper pDataMapper)
  {
    assert getContainers() != null;
    getContainers().getDataMappers().add(new BeanDataMapper(pDataMapper));
  }

  /**
   * Adds a temporary data mapper, which only applies to a single field, to this data core.
   *
   * @param pDataMapper the data mapper
   */
  default <TYPE> void addDataMapperForField(IField<TYPE> pField, ISingleFieldFlatDataMapper<TYPE> pDataMapper)
  {
    assert getContainers() != null;
    getContainers().getDataMappers().add(new BeanDataMapper(pDataMapper, pField));
  }

  /**
   * Removes a specific data mappers from this data core.
   * The method can be used for normal mappers and single field mappers.
   *
   * @param pDataMapper the data mapper to remove
   * @return <tt>true</tt>, if the mapper has been removed successfully
   */
  default boolean removeDataMapper(IBeanFlatDataMapper pDataMapper)
  {
    assert getContainers() != null;
    return getContainers().getDataMappers().removeIf(pMapper -> pMapper.getDataMapper() == pDataMapper);
  }

  /**
   * Clears all data mappers (normal and single) from this data core.
   */
  default void clearDataMappers()
  {
    assert getContainers() != null;
    getContainers().getDataMappers().clear();
  }

  /**
   * Determines if this core contains a certain bean field.
   *
   * @param pField the bean field
   * @param <TYPE> the data type of the field
   * @return <tt>true</tt>, if the field is present
   */
  default <TYPE> boolean containsField(IField<TYPE> pField)
  {
    return streamFields()
        .anyMatch(pExistingBean -> pExistingBean == pField); //Compare references because of the static definition of bean fields
  }

  /**
   * A hierarchical reference structure for this core / bean.
   * The structure contains direct and deep parent references to this bean.
   *
   * @return a hierarchical reference structure
   * @see IHierarchicalBeanStructure
   */
  @Override
  default IHierarchicalBeanStructure getHierarchicalStructure()
  {
    return new HierarchicalBeanStructureImpl<>(this);
  }

  /**
   * Default implementation of the {@link IHierarchicalBeanStructure}.
   * Just defines a class extending the base implementation and implementing the interface.
   * The extra functionality is provided via default methods.
   */
  class HierarchicalBeanStructureImpl<C, B extends IBean<B>, L extends IBeanChangeListener<B>>
      extends HierarchicalStructureImpl<C, B, L, BeanEncapsulatedContainers<B, L>> implements IHierarchicalBeanStructure
  {
    public HierarchicalBeanStructureImpl(IEncapsulated<C, B, L, BeanEncapsulatedContainers<B, L>> pEncapsulated)
    {
      super(pEncapsulated);
    }
  }
}
