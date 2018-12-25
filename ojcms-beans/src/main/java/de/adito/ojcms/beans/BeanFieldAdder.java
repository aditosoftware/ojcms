package de.adito.ojcms.beans;

import de.adito.ojcms.beans.fields.IField;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.*;

/**
 * Utility to add bean fields.
 *
 * @param <FIELD> the runtime type of the field to add/create
 * @author Simon Danner, 25.12.2018
 */
public final class BeanFieldAdder<FIELD extends IField>
{
  private final BiConsumer<FIELD, Integer> addFunction;
  private final Supplier<Integer> fieldCountSupplier;
  private final Class<FIELD> beanFieldType;
  private final String fieldName;
  private final Collection<Annotation> annotations;
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
  BeanFieldAdder(BiConsumer<FIELD, Integer> pAddFunction, Supplier<Integer> pFieldCountSupplier, Class<FIELD> pBeanFieldType,
                 String pFieldName, Collection<Annotation> pAnnotations)
  {
    addFunction = pAddFunction;
    fieldCountSupplier = pFieldCountSupplier;
    beanFieldType = pBeanFieldType;
    fieldName = pFieldName;
    annotations = pAnnotations;
  }

  /**
   * Sets the generic type of the field to add.
   * This is necessary if the field uses a generic type as the field's data type directly.
   *
   * @param pGenericType the generic type of the field to add
   * @return the bean field adder itself to enable a pipelining mechanism
   */
  public BeanFieldAdder<FIELD> withGenericType(Class<?> pGenericType)
  {
    genericType = pGenericType;
    return this;
  }

  /**
   * Adds the new field at the end of the fields (order of declaration).
   *
   * @return the added field
   */
  public FIELD addAtTheEnd()
  {
    return addAtIndex(fieldCountSupplier.get());
  }

  /**
   * Adds the new field at a certain index (order of declaration).
   *
   * @param pIndex the index to add the field at
   * @return the added field
   */
  public FIELD addAtIndex(int pIndex)
  {
    //noinspection unchecked
    final FIELD field = (FIELD) BeanFieldFactory.createField(beanFieldType, () -> genericType, fieldName, annotations);
    addFunction.accept(field, pIndex);
    return field;
  }
}
