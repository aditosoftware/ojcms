package de.adito.ojcms.beans;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.utils.StringUtility;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.*;

/**
 * Utility to add bean fields.
 *
 * @param <BEAN>  the runtime type of the bean the field is for
 * @param <VALUE> the data type of the field to add
 * @param <FIELD> the runtime type of the field to add/create
 * @author Simon Danner, 25.12.2018
 */
public final class BeanFieldAdder<BEAN extends IBean<BEAN>, VALUE, FIELD extends IField<VALUE>>
{
  private final ObjIntConsumer<FIELD> addFunction;
  private final IntSupplier fieldCountSupplier;
  private final Class<FIELD> beanFieldType;
  private final String fieldName;
  private final Collection<Annotation> annotations;
  private Optional<BiPredicate<BEAN, VALUE>> activeCondition = Optional.empty();
  private Class<?> genericType;

  /**
   * Creates the field adder.
   *
   * @param pAddFunction        a function to add the created field at a certain index
   * @param pFieldCountSupplier a supplier for the current field count of the bean to add the field
   * @param pBeanFieldType      the type of the field to create
   * @param pFieldName          the name of the field to create
   * @param pAnnotations        the annotations of the field to create
   */
  BeanFieldAdder(ObjIntConsumer<FIELD> pAddFunction, IntSupplier pFieldCountSupplier, Class<FIELD> pBeanFieldType, String pFieldName,
                 Collection<Annotation> pAnnotations)
  {
    addFunction = Objects.requireNonNull(pAddFunction);
    fieldCountSupplier = Objects.requireNonNull(pFieldCountSupplier);
    beanFieldType = Objects.requireNonNull(pBeanFieldType);
    fieldName = StringUtility.requireNotEmpty(pFieldName, "field name");
    annotations = Objects.requireNonNull(pAnnotations);
  }

  /**
   * Sets the generic type of the field to add.
   * This is necessary if the field uses a generic type as the field's data type directly.
   *
   * @param pGenericType the generic type of the field to add
   * @return the bean field adder itself to enable a pipelining mechanism
   */
  public BeanFieldAdder<BEAN, VALUE, FIELD> withGenericType(Class<?> pGenericType)
  {
    genericType = pGenericType;
    return this;
  }

  /**
   * Declares the field as optional field to be active under a certain predicate only.
   *
   * @param pActiveCondition the condition to determine the active state of the field
   * @return the bean field adder itself to enable a pipelining mechanism
   */
  public BeanFieldAdder<BEAN, VALUE, FIELD> optionalField(BiPredicate<BEAN, VALUE> pActiveCondition)
  {
    activeCondition = Optional.of(pActiveCondition);
    return this;
  }

  /**
   * Adds the new field at the end of the fields (order of declaration).
   *
   * @return the added field
   */
  public FIELD addAtTheEnd()
  {
    return addAtIndex(fieldCountSupplier.getAsInt());
  }

  /**
   * Adds the new field at a certain index (order of declaration).
   *
   * @param pIndex the index to add the field at
   * @return the added field
   */
  public FIELD addAtIndex(int pIndex)
  {
    final FIELD field = BeanFieldFactory.createField(beanFieldType, () -> genericType, fieldName, annotations, activeCondition);
    addFunction.accept(field, pIndex);
    return field;
  }
}
