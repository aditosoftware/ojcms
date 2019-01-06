package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDuplicateException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.reactive.events.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.adito.ojcms.beans.BeanInternalEvents.*;

/**
 * A modifiable bean with dynamical fields.
 * Allows the extension and removal of bean fields.
 *
 * @param <BEAN> the runtime type of the concrete bean
 * @author Simon Danner, 01.02.2017
 */
@RequiresEncapsulatedAccess
public interface IModifiableBean<BEAN extends IModifiableBean<BEAN>> extends IBean<BEAN>
{
  /**
   * Extends this bean by one field.
   * A new field instance will be created.
   *
   * @param pFieldType   the new field's data type
   * @param pName        the new field's name
   * @param pAnnotations the new field's annotations
   * @param <VALUE>      the data type of the field to add
   * @param <FIELD>      the runtime type of the field to add/create
   * @return the created field instance
   */
  default <VALUE, FIELD extends IField<VALUE>> BeanFieldAdder<BEAN, VALUE, FIELD> fieldAdder(Class<FIELD> pFieldType, String pName,
                                                                                             Collection<Annotation> pAnnotations)
  {
    final IEncapsulatedBeanData encapsulated = requestEncapsulatedData(this);
    if (encapsulated.streamFields().anyMatch(pField -> pField.getName().equals(pName)))
      throw new BeanFieldDuplicateException(pName);
    return new BeanFieldAdder<>(this::addFieldAtIndex, encapsulated::getFieldCount, pFieldType, pName, pAnnotations);
  }

  /**
   * Extends this bean by an already existing field instance.
   *
   * @param pField  the field to add
   * @param <VALUE> the field's data type
   */
  default <VALUE> void addField(IField<VALUE> pField)
  {
    addFieldAtIndex(pField, requestEncapsulatedData(this).getFieldCount());
  }

  /**
   * Extends this bean by an already existing field instance at a certain index.
   *
   * @param pField  the field to add
   * @param pIndex  the index of the field (includes private fields)
   * @param <VALUE> the field's data type
   */
  default <VALUE> void addFieldAtIndex(IField<VALUE> pField, int pIndex)
  {
    final IEncapsulatedBeanData encapsulated = requestEncapsulatedData(this);
    if (encapsulated.containsField(pField))
      throw new BeanFieldDuplicateException(pField.getName());
    encapsulated.addField(pField, pIndex);
    if (getFieldActivePredicate().isOptionalActive(pField))
      //noinspection unchecked
      propagateChange(new BeanFieldAddition<>((BEAN) this, pField));
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
    final IEncapsulatedBeanData encapsulated = requestEncapsulatedDataForField(this, pField);
    final VALUE oldValue = encapsulated.getValue(pField);
    encapsulated.removeField(pField);
    //noinspection unchecked
    propagateChange(new BeanFieldRemoval<>((BEAN) this, pField, oldValue));
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
    return removeField(getFieldByName(pFieldName));
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
