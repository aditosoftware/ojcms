package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.GenericBeanField;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.literals.IAdditionalMemberInfo;
import de.adito.ojcms.beans.literals.fields.IField;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;

/**
 * A static factory to create bean fields.
 * Fields should only be created via the methods of this class.
 * They take care of some initialization work.
 *
 * @author Simon Danner, 23.08.2016
 */
final class BeanFieldFactory
{
  static final IAdditionalMemberInfo<BiPredicate> OPTIONAL_FIELD_INFO = () -> BiPredicate.class;

  private BeanFieldFactory()
  {
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   *
   * @param pFieldType       the field's type
   * @param pName            the field's name
   * @param pIsPrivate       determines if the field is declared privately
   * @param pAnnotations     the field's annotations
   * @param pActiveCondition an optional condition for optional bean fields (determines the active state of the field)
   * @param <VALUE>          the data type of the field to create
   * @param <FIELD>          the generic field type
   * @return the newly created field
   */
  static <VALUE, FIELD extends IField<VALUE>> FIELD createField(Class<FIELD> pFieldType, String pName, boolean pIsPrivate,
                                                                Collection<Annotation> pAnnotations,
                                                                @Nullable BiPredicate<? extends IBean, VALUE> pActiveCondition)
  {
    return createField(pFieldType, () -> null, pName, pIsPrivate, pAnnotations, pActiveCondition);
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
   * @param pIsPrivate           determines if the field is declared privately
   * @param pAnnotations         the field's annotations
   * @param pActiveCondition     an optional condition for optional bean fields (determines the active state of the field)
   * @param <VALUE>              the data type of the field to create
   * @param <FIELD>              the generic field type
   * @return the newly created field
   */
  static <VALUE, FIELD extends IField<VALUE>> FIELD createField(Class<FIELD> pFieldType, Supplier<Class<?>> pGenericTypeSupplier,
                                                                String pName, boolean pIsPrivate, Collection<Annotation> pAnnotations,
                                                                @Nullable BiPredicate<? extends IBean, VALUE> pActiveCondition)
  {
    try
    {
      final Optional<Class<?>> optionalGenericType = _getGenericType(pFieldType, pGenericTypeSupplier);
      final boolean isOptional = pActiveCondition != null;
      //Constructor argument distinction between generic and non generic values (generic types provide their type additionally)
      final Class[] constructorArgumentTypes = optionalGenericType
          .map(pType -> new Class[]{Class.class, String.class, Collection.class, boolean.class, boolean.class})
          .orElseGet(() -> new Class[]{String.class, Collection.class, boolean.class, boolean.class});

      final Object[] constructorArguments = optionalGenericType
          .map(pClass -> new Object[]{pClass, pName, pAnnotations, isOptional, pIsPrivate})
          .orElseGet(() -> new Object[]{pName, pAnnotations, isOptional, pIsPrivate});

      //Create the field by using the reflected constructor
      final Constructor<FIELD> constructor = pFieldType.getDeclaredConstructor(constructorArgumentTypes);
      if (!constructor.isAccessible())
        constructor.setAccessible(true);

      final FIELD field = constructor.newInstance(constructorArguments);

      //Add the optional condition as field info to determine the state of the field later on
      if (isOptional)
        field.addAdditionalInformation(OPTIONAL_FIELD_INFO, pActiveCondition);

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
}
