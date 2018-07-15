package de.adito.beans.core;

import de.adito.beans.core.util.BeanUtil;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
      throw new RuntimeException("A field with the name '" + pName + "' is already existing at this bean!");
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
    if (hasField(pField))
      throw new RuntimeException("A bean cannot have the same field twice! field: " + pField.getName());
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
   * Removes a field by its name.
   *
   * @param pFieldName the name of the field to remove
   */
  default void removeFieldByName(String pFieldName)
  {
    removeField(BeanUtil.findFieldByName(this, pFieldName));
  }

  /**
   * Removes all fields that apply to a given predicate.
   *
   * @param pFieldPredicate the field predicate that determines which fields should be retained
   */
  default void removeFieldIf(Predicate<IField<?>> pFieldPredicate)
  {
    List<IField<?>> toRemove = streamFields()
        .filter(pField -> !pFieldPredicate.test(pField))
        .collect(Collectors.toList());
    toRemove.forEach(this::removeField);
  }
}
