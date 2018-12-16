package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import de.adito.ojcms.beans.statistics.IStatisticData;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Encapsulated bean data core.
 * It contains multiple combinations of bean fields and associated values.
 *
 * @author Simon Danner, 20.01.2017
 */
@EncapsulatedData
interface IEncapsulatedBeanData extends IEncapsulatedData<FieldValueTuple<?>, IBeanDataSource>
{
  /**
   * The value for a bean field.
   *
   * @param pField  the bean field
   * @param <VALUE> the data type of the field
   * @return the value for the field
   */
  <VALUE> VALUE getValue(IField<VALUE> pField);

  /**
   * Sets a value for a bean field.
   *
   * @param pField  the bean field
   * @param pValue  the new value
   * @param <VALUE> the data type of the field
   */
  <VALUE> void setValue(IField<VALUE> pField, VALUE pValue);

  /**
   * Adds a bean field to the data core.
   *
   * @param pField  the bean field to add
   * @param pIndex  the index at which the field should be added (the index includes private fields)
   * @param <VALUE> the data type of the field
   */
  <VALUE> void addField(IField<VALUE> pField, int pIndex);

  /**
   * Removes a bean field from the data core.
   *
   * @param pField  the bean field to remove
   * @param <VALUE> the data type of the field
   */
  <VALUE> void removeField(IField<VALUE> pField);

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
  Map<IField<?>, IStatisticData<?>> getStatisticData();

  /**
   * A stream of bean fields of this data core.
   *
   * @return a stream of bean fields
   */
  Stream<IField<?>> streamFields();

  /**
   * Determines, if this data core contains a certain bean field.
   *
   * @param pField  the bean field
   * @param <VALUE> the data type of the field
   * @return <tt>true</tt>, if the field is present
   */
  default <VALUE> boolean containsField(IField<VALUE> pField)
  {
    return streamFields()
        .anyMatch(pExistingBean -> pExistingBean == pField); //Compare references because of the static definition of bean fields
  }
}
