package de.adito.beans.core;

import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalBeanStructure;
import de.adito.beans.core.statistics.IStatisticData;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Defines the data core for a bean.
 * It contains a collection of key-value pairs, which represent the fields and their associated values.
 *
 * @param <BEAN> the generic bean type that uses this data core
 * @author Simon Danner, 20.01.2017
 */
interface IBeanEncapsulated<BEAN extends IBean<BEAN>> extends IEncapsulated<FieldTuple<?>, BEAN, IBeanChangeListener<BEAN>>
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
   * @param pIndex the index at which the field should be added
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
   * The index of a certain bean field.
   * The index depends in the definition order of the fields.
   *
   * @param pField the bean field field
   * @param <TYPE> the data type of the field
   * @return the index of the field
   */
  <TYPE> int getFieldIndex(IField<TYPE> pField);

  /**
   * The amount of bean fields of this data core.
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
  class HierarchicalBeanStructureImpl<C, B extends IBean<B>, L extends IBeanChangeListener<B>> extends HierarchicalStructureImpl<C, B, L>
      implements IHierarchicalBeanStructure
  {
    public HierarchicalBeanStructureImpl(IEncapsulated<C, B, L> pEncapsulated)
    {
      super(pEncapsulated);
    }
  }
}
