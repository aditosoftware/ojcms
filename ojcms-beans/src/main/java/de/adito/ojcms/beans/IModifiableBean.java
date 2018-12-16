package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.reactive.events.*;
import de.adito.ojcms.beans.util.BeanUtil;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A modifiable bean with dynamical fields.
 * Allows the extension and removal of bean fields.
 *
 * @param <BEAN> the runtime type of the concrete bean
 * @author Simon Danner, 01.02.2017
 */
@RequiresEncapsulatedAccess
public interface IModifiableBean<BEAN extends IBean<BEAN>> extends IBean<BEAN>
{
  /**
   * Extends this bean by one field.
   * A new field instance will be created.
   *
   * @param pFieldType   the new field's data type
   * @param pName        the new field's name
   * @param pAnnotations the new field's annotations
   * @param <VALUE>      the generic data type of the new field
   * @return the created field instance
   */
  default <VALUE, FIELD extends IField<VALUE>> FIELD addField(Class<FIELD> pFieldType, String pName, Collection<Annotation> pAnnotations)
  {
    return addField(pFieldType, pName, pAnnotations, -1);
  }

  /**
   * Extends this bean by one field.
   * A new field instance will be created.
   *
   * @param pFieldType   the new field's data type
   * @param pName        the new field's name
   * @param pAnnotations the new field's annotations
   * @param pIndex       the index to add the field, or -1 to put the field at the end (includes private fields)
   * @param <VALUE>      the generic data type of the new field
   * @return the created field instance
   */
  default <VALUE, FIELD extends IField<VALUE>> FIELD addField(Class<FIELD> pFieldType, String pName,
                                                              Collection<Annotation> pAnnotations, int pIndex)
  {
    final IEncapsulatedBeanData encapsulated = getEncapsulatedData();
    assert encapsulated != null;
    if (encapsulated.streamFields().anyMatch(pField -> pField.getName().equals(pName)))
      throw new RuntimeException("A field with the name '" + pName + "' is already existing at this bean!");
    final FIELD newField = BeanFieldFactory.createField(pFieldType, pName, pAnnotations);
    if (pIndex == -1)
      addField(newField);
    else
      addField(newField, pIndex);
    return newField;
  }

  /**
   * Extends this bean by a already existing field instance.
   *
   * @param pField  the field to add
   * @param <VALUE> the field's data type
   */
  default <VALUE> void addField(IField<VALUE> pField)
  {
    addField(pField, getEncapsulatedData().getFieldCount());
  }

  /**
   * Extends this bean by a already existing field instance at a certain index.
   *
   * @param pField  the field to add
   * @param pIndex  the index of the field (includes private fields)
   * @param <VALUE> the field's data type
   */
  default <VALUE> void addField(IField<VALUE> pField, int pIndex)
  {
    final IEncapsulatedBeanData encapsulated = getEncapsulatedData();
    assert encapsulated != null;
    if (encapsulated.containsField(pField))
      throw new RuntimeException("A bean cannot have the same field twice! field: " + pField.getName());
    encapsulated.addField(pField, pIndex);
    if (getFieldActivePredicate().isOptionalActive(pField))
      //noinspection unchecked
      BeanEvents.propagate(new BeanFieldAddition<>((BEAN) this, pField));
  }

  /**
   * Removes a field from this bean.
   *
   * @param pField  the field to remove
   * @param <VALUE> the field's data type
   * @return the value of the field before its removal
   */
  default <VALUE> VALUE removeField(IField<VALUE> pField)
  {
    final IEncapsulatedBeanData encapsulated = getEncapsulatedData();
    assert encapsulated != null;
    final VALUE oldValue = encapsulated.getValue(pField);
    encapsulated.removeField(pField);
    //noinspection unchecked
    BeanEvents.propagate(new BeanFieldRemoval<>((BEAN) this, pField, oldValue));
    return oldValue;
  }

  /**
   * Removes a field by its name.
   *
   * @param pFieldName the name of the field to remove
   * @return the value of the field before its removal
   */
  default Object removeFieldByName(String pFieldName)
  {
    return removeField(BeanUtil.findFieldByName(this, pFieldName));
  }

  /**
   * Removes all fields that apply to a given predicate.
   *
   * @param pFieldPredicate the field predicate that determines which fields should be removed
   */
  default void removeFieldIf(Predicate<IField<?>> pFieldPredicate)
  {
    final List<IField<?>> toRemove = streamFields()
        .filter(pFieldPredicate)
        .collect(Collectors.toList());
    toRemove.forEach(this::removeField);
  }
}
