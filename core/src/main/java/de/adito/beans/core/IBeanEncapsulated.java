package de.adito.beans.core;

import de.adito.beans.core.fields.util.FieldTuple;
import de.adito.beans.core.mappers.*;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.IBeanFieldPredicate;

import java.util.*;
import java.util.stream.Stream;

/**
 * Defines the data core for a bean.
 * It contains a collection of field value tuples.
 *
 * @author Simon Danner, 20.01.2017
 */
interface IBeanEncapsulated extends IEncapsulated<FieldTuple<?>>, IReferable
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
  void addFieldFilter(IBeanFieldPredicate pPredicate);

  /**
   * Removes a field filter from this data core.
   *
   * @param pPredicate the predicate/filter to remove
   */
  void removeFieldFilter(IBeanFieldPredicate pPredicate);

  /**
   * Clears all field filters.
   */
  void clearFieldFilters();

  /**
   * Adds a temporary data mapper to this data core.
   *
   * @param pDataMapper the data mapper
   */
  void addDataMapper(IBeanFlatDataMapper pDataMapper);

  /**
   * Adds a temporary data mapper, which only applies to a single field, to this data core.
   *
   * @param pDataMapper the data mapper
   */
  <TYPE> void addDataMapperForField(IField<TYPE> pField, ISingleFieldFlatDataMapper<TYPE> pDataMapper);

  /**
   * Removes a specific data mappers from this data core.
   * The method can be used for normal mappers and single field mappers.
   *
   * @param pDataMapper the data mapper to remove
   * @return <tt>true</tt>, if the mapper has been removed successfully
   */
  boolean removeDataMapper(IBeanFlatDataMapper pDataMapper);

  /**
   * Clears all data mappers (normal and single) from this data core.
   */
  void clearDataMappers();

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
}
