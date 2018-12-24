package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.OptionalField;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.exceptions.*;
import de.adito.ojcms.beans.exceptions.field.BeanFieldCreationException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.picoservice.IPicoRegistry;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * A static factory to create bean fields.
 * Fields should only be created via the methods of this class.
 * They take care of some initialization work.
 *
 * All specific beans of the application should use this class to create their fields.
 * For an example take a look at {@link Bean}.
 *
 * @author Simon Danner, 23.08.2016
 */
public final class BeanFieldFactory
{
  private static Map<Class, Class<? extends IField>> typeFieldMapping;

  private BeanFieldFactory()
  {
  }

  /**
   * Takes a look at all static bean fields of a certain class and creates the first not initialized field automatically.
   * In this way all bean fields can be created through this method.
   * Usage: "public static final TextField name = BeanFieldFactory.create(CLASSNAME.class);"
   * This method takes care about the whole initialization of the certain field. (name, type, annotations, etc.)
   *
   * @param pBeanType the bean type to which the created field should belong to
   * @param <BEAN>    the generic type of the parameter above
   *                  (is here based on the concrete {@link Bean} class rather than on the interface, so it can not be a transformed bean type
   * @param <FIELD>   the generic type of the field that will be created
   * @return the newly created field instance
   */
  @SuppressWarnings("unchecked")
  public static <BEAN extends Bean<BEAN>, FIELD extends IField> FIELD create(Class<BEAN> pBeanType)
  {
    final Field toCreate = BeanReflector.reflectDeclaredBeanFields(pBeanType).stream()
        .filter(pField -> {
          try
          {
            return pField.get(null) == null;
          }
          catch (IllegalAccessException pE)
          {
            throw new OJInternalException(pE);
          }
        })
        .findAny()
        .orElseThrow(() -> new BeanFieldCreationException(pBeanType));
    final Class<FIELD> fieldType = (Class<FIELD>) toCreate.getType();
    return (FIELD) createField(fieldType, _getGenType(toCreate, fieldType), toCreate.getName(), Arrays.asList(toCreate.getAnnotations()));
  }

  /**
   * Provides the bean field type for a certain inner data type.
   * This depends on the field types annotated with {@link TypeDefaultField}.
   * They determine what field type is the default for the searched data type.
   *
   * @param pType   the inner data type of a field
   * @param <VALUE> the generic data type
   * @return the default field type for this data type
   */
  static <VALUE> Class<IField<VALUE>> getFieldTypeFromType(Class<VALUE> pType)
  {
    if (typeFieldMapping == null)
      typeFieldMapping = IPicoRegistry.INSTANCE.find(IField.class, TypeDefaultField.class).entrySet().stream()
          .flatMap(pEntry -> Stream.of(pEntry.getValue().types())
              .map(pDataType -> new AbstractMap.SimpleEntry<>(pDataType, pEntry.getKey())))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (pFieldType1, pFieldType2) ->
                                    {
                                      throw new OJInternalException("Incorrect default data types for bean field: " + pFieldType1.getSimpleName() +
                                                                        " supports the same data type as " + pFieldType2.getSimpleName());
                                    }));

    if (!typeFieldMapping.containsKey(pType))
      throw new OJInternalException("There is no bean field for this data type: " + pType.getSimpleName());
    //noinspection unchecked
    return (Class<IField<VALUE>>) typeFieldMapping.get(pType);
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   * This method should only be used internally within this package.
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
    return createField(pFieldType, Optional.empty(), pName, pAnnotations);
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   * It's also possible to provide an additional generic type of the bean field.
   * For example, this may be the bean type of a container field.
   *
   * @param pFieldType   the field's type
   * @param pGenType     the field's generic type (NOT the same type of the field as generic)
   * @param pName        the field's name
   * @param pAnnotations the field's annotations
   * @param <VALUE>      the field's data type
   * @param <FIELD>      the generic field type
   * @return the newly created field
   */
  static <VALUE, FIELD extends IField<VALUE>> FIELD createField(Class<FIELD> pFieldType, Optional<Class> pGenType, String pName,
                                                                Collection<Annotation> pAnnotations)
  {
    try
    {
      final Class[] constructorArgTypes = pGenType.map(pType -> new Class[]{Class.class, String.class, Collection.class})
          .orElseGet(() -> new Class[]{String.class, Collection.class});
      final Object[] constructorArgs = pGenType.map(pClass -> new Object[]{pClass, pName, pAnnotations})
          .orElseGet(() -> new Object[]{pName, pAnnotations});
      final FIELD field = pFieldType.getConstructor(constructorArgTypes).newInstance(constructorArgs);
      _checkOptionalField(field);
      return field;
    }
    catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException pE)
    {
      throw new OJInternalException(pE);
    }
  }

  /**
   * Evaluates an optional generic type of a bean field.
   *
   * @param pField     the field instance from the reflection API
   * @param pFieldType the type of the bean field
   * @param <FIELD>    the type of the bean field as generic
   * @return an optional generic type of the field instance
   */
  private static <FIELD extends IField> Optional<Class> _getGenType(Field pField, Class<FIELD> pFieldType)
  {
    final Type genericType = pField.getGenericType();
    if (!(genericType instanceof ParameterizedType))
      return Optional.empty();

    final Type fieldSuperType = pFieldType.getGenericSuperclass();
    if (!(fieldSuperType instanceof ParameterizedType))
      return Optional.empty();

    final Type fieldSuperGenType = ((ParameterizedType) fieldSuperType).getActualTypeArguments()[0];
    return Optional.of((Class) (fieldSuperGenType instanceof ParameterizedType ? ((ParameterizedType) fieldSuperGenType).getRawType() :
        ((ParameterizedType) genericType).getActualTypeArguments()[0]));
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
