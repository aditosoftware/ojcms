package de.adito.beans.core;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;

/**
 * A modifiable bean with dynamical fields.
 * Allows the extension and removal of bean fields.
 *
 * @param <BEAN> the generic type of the concrete bean that implements this interface
 * @author Simon Danner, 01.02.2017
 */
public interface IModifiableBean<BEAN extends IBean<BEAN>> extends IBean<BEAN>
{
  /**
   * Extends this bean by one field.
   * A new field instance will be created.
   *
   * @param pFieldType   the new field's data type
   * @param pName        the new field's name
   * @param pAnnotations the new field's annotations
   * @param <TYPE>       the generic data type of the new field
   * @return the created field instance
   */
  default <TYPE, FIELD extends IField<TYPE>> FIELD addField(Class<FIELD> pFieldType, String pName, Collection<Annotation> pAnnotations)
  {
    IBeanEncapsulated<BEAN> encapsulated = getEncapsulated();
    assert encapsulated != null;
    if (encapsulated.streamFields().anyMatch(pField -> pField.getName().equals(pName)))
      throw new RuntimeException("field: " + pName);
    FIELD newField = BeanFieldFactory.createField(pFieldType, pName, pAnnotations);
    addField(newField);
    return newField;
  }

  /**
   * Extends this bean by a already existing field instance.
   *
   * @param pField the field to add
   * @param <TYPE> the field's data type
   */
  default <TYPE> void addField(IField<TYPE> pField)
  {
    addField(pField, getFieldCount());
  }

  /**
   * Extends this bean by a already existing field instance at a certain index.
   *
   * @param pField the field to add
   * @param pIndex the index
   * @param <TYPE> the field's data type
   */
  default <TYPE> void addField(IField<TYPE> pField, int pIndex)
  {
    IBeanEncapsulated<BEAN> encapsulated = getEncapsulated();
    assert encapsulated != null;
    encapsulated.addField(pField, pIndex);
    if (getFieldActiveSupplier().isOptionalActive(pField))
      //noinspection unchecked
      encapsulated.fire(pListener -> pListener.fieldAdded((BEAN) this, pField));
  }

  /**
   * Removes a field from this bean.
   *
   * @param pField the field to remove
   * @param <TYPE> the field's data type
   */
  default <TYPE> void removeField(IField<TYPE> pField)
  {
    IBeanEncapsulated<BEAN> encapsulated = getEncapsulated();
    assert encapsulated != null;
    TYPE oldValue = encapsulated.getValue(pField);
    encapsulated.removeField(pField);
    //noinspection unchecked
    encapsulated.fire(pListener -> pListener.fieldRemoved((BEAN) this, pField, oldValue));
  }

  /**
   * Removes all fields that apply to a given predicate.
   *
   * @param pFieldPredicate the field predicate that determines which fields should be removed
   * @param <TYPE>          the field's data type
   */
  default <TYPE> void removeFieldIf(Predicate<IField<TYPE>> pFieldPredicate)
  {
    Iterator<Map.Entry<IField<?>, Object>> it = getEncapsulated().iterator();
    while (it.hasNext())
      //noinspection unchecked
      if (pFieldPredicate.test((IField<TYPE>) it.next().getKey()))
        it.remove();
  }
}
