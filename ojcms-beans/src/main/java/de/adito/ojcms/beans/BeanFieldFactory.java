package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.fields.IField;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * A static factory to create bean fields.
 * Fields should only be created via the methods of this class.
 * They take care of some initialization work.
 *
 * @author Simon Danner, 23.08.2016
 */
final class BeanFieldFactory
{
  private BeanFieldFactory()
  {
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   *
   * @param pFieldType   the field's type
   * @param pName        the field's name
   * @param pAnnotations the field's annotations
   * @param <VALUE>      the field's data type
   * @param <FIELD>      the generic field type
   * @return the newly created field
   */
  static <VALUE, FIELD extends IField<VALUE>> FIELD createField(Class<FIELD> pFieldType, String pName, Collection<Annotation> pAnnotations)
  {
    return createField(pFieldType, () -> null, pName, pAnnotations);
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   * It's also possible to provide an additional generic type of the bean field.
   *
   * @param pFieldType           the field's type
   * @param pGenericTypeSupplier a supplier for an optional generic type for the field type. (null if not present)
   *                             this is necessary if fields use an generic type as the field's data value directly.
   *                             see {@link de.adito.ojcms.beans.annotations.GenericBeanField}
   * @param pName                the field's name
   * @param pAnnotations         the field's annotations
   * @param <VALUE>              the field's data type
   * @param <FIELD>              the generic field type
   * @return the newly created field
   */
  static <VALUE, FIELD extends IField<VALUE>> FIELD createField(Class<FIELD> pFieldType, Supplier<Class<?>> pGenericTypeSupplier,
                                                                String pName, Collection<Annotation> pAnnotations)
  {
    try
    {
      final Optional<Class<?>> optionalGenericType = _getGenericType(pFieldType, pGenericTypeSupplier);
      final Class[] constructorArgTypes = optionalGenericType.map(pType -> new Class[]{Class.class, String.class, Collection.class})
          .orElseGet(() -> new Class[]{String.class, Collection.class});
      final Object[] constructorArgs = optionalGenericType.map(pClass -> new Object[]{pClass, pName, pAnnotations})
          .orElseGet(() -> new Object[]{pName, pAnnotations});
      final Constructor<FIELD> constructor = pFieldType.getDeclaredConstructor(constructorArgTypes);
      if (!constructor.isAccessible())
        constructor.setAccessible(true);
      final FIELD field = constructor.newInstance(constructorArgs);
      _checkOptionalField(field);
      return field;
    }
    catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException pE)
    {
      throw new OJInternalException(pE);
    }
  }

  /**
   * An optional generic type for the bean field to create.
   * All generic bean field types should be annotated by {@link GenericBeanField}.
   * If {@link GenericBeanField#genericWrapperType()} is set, this type will be used.
   * Otherwise the given supplier will define the value if necessary.
   *
   * @param pFieldType           the bean field type to create an instance
   * @param pGenericTypeSupplier the supplier for the generic type of the field, if it uses the generic type as data type directly
   * @return an optional generic type for the field to create
   */
  private static Optional<Class<?>> _getGenericType(Class<? extends IField> pFieldType, Supplier<Class<?>> pGenericTypeSupplier)
  {
    if (!pFieldType.isAnnotationPresent(GenericBeanField.class))
      return Optional.empty();

    final Class<?> wrapperType = pFieldType.getAnnotation(GenericBeanField.class).genericWrapperType();
    return wrapperType != void.class ? Optional.of(wrapperType) : Optional.ofNullable(pGenericTypeSupplier.get());
  }

  /**
   * Checks, if a bean field is marked (via {@link OptionalField}) as optional field.
   * In this case the condition, which is defined through the annotation, will be stored as an additional information at the field.
   * So this information can be used later to determine if the field is active at any moment.
   *
   * @param pField the bean field to check
   */
  private static void _checkOptionalField(IField<?> pField) throws IllegalAccessException, InstantiationException, NoSuchMethodException,
      InvocationTargetException
  {
    if (!pField.isOptional())
      return;

    final OptionalField optional = pField.getAnnotationOrThrow(OptionalField.class);
    final Constructor<? extends OptionalField.IActiveCondition> constructor = optional.value().getDeclaredConstructor();
    if (!constructor.isAccessible())
      constructor.setAccessible(true);
    final OptionalField.IActiveCondition<?> activeCondition = constructor.newInstance();
    pField.addAdditionalInformation(OptionalField.ACTIVE_CONDITION, activeCondition);
  }
}
