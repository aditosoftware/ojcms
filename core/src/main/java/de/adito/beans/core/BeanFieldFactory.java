package de.adito.beans.core;

import de.adito.beans.core.annotations.*;
import de.adito.beans.core.util.BeanReflector;
import de.adito.picoservice.IPicoRegistry;
import org.jetbrains.annotations.Nullable;

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
 * For an example, take a look at the {@link Bean}.
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
    Field toCreate = BeanReflector.reflectDeclaredBeanFields(pBeanType).stream()
        .filter(pField -> {
          try
          {
            return pField.get(null) == null;
          }
          catch (IllegalAccessException pE)
          {
            throw new RuntimeException(pE);
          }
        })
        .findAny()
        .orElseThrow(() -> new RuntimeException("Unable to create field. There are no static fields or all of them are initialized already." +
                                                    "bean-type: " + pBeanType.getName()));
    Class<FIELD> fieldType = (Class<FIELD>) toCreate.getType();
    return (FIELD) createField(fieldType, _getGenType(toCreate, fieldType), toCreate.getName(), Arrays.asList(toCreate.getAnnotations()));
  }

  /**
   * Provides the bean field type for a certain inner data type.
   * This depends on the field types annotated with {@link TypeDefaultField}.
   * They determine, what field type is the default for the searched data type.
   *
   * @param pType  the inner data type of a field
   * @param <TYPE> the generic data type
   * @return the default field type for this data type
   */
  public static <TYPE> Class<IField<TYPE>> getFieldTypeFromType(Class<TYPE> pType)
  {
    if (typeFieldMapping == null)
      typeFieldMapping = IPicoRegistry.INSTANCE.find(IField.class, TypeDefaultField.class).entrySet().stream()
          .flatMap(pEntry -> Stream.of(pEntry.getValue().types())
              .map(pDataType -> new AbstractMap.SimpleEntry<>(pDataType, pEntry.getKey())))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                    (pFieldType1, pFieldType2) ->
                                    {
                                      throw new RuntimeException("Incorrect default data types for bean fields: " + pFieldType1.getSimpleName() +
                                                                     " supports the same data type as " + pFieldType2.getSimpleName());
                                    }));

    if (!typeFieldMapping.containsKey(pType))
      throw new RuntimeException("There is no bean field for this data type: " + pType.getSimpleName());
    //noinspection unchecked
    return (Class<IField<TYPE>>) typeFieldMapping.get(pType);
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   * This method should only be used internally within this package.
   *
   * @param pFieldType   the fields's type
   * @param pName        the field's name
   * @param pAnnotations the field's annotations
   * @param <TYPE>       the field's data type
   * @param <FIELD>      the generic field type
   * @return the newly created field
   */
  static <TYPE, FIELD extends IField<TYPE>> FIELD createField(Class<FIELD> pFieldType, String pName, Collection<Annotation> pAnnotations)
  {
    return createField(pFieldType, null, pName, pAnnotations);
  }

  /**
   * Creates a new bean field based on a certain type und some initial data.
   * It's also possible to provide an additional generic type of the bean field.
   * For example, this may be the bean type of a container field.
   *
   * @param pFieldType   the fields's type
   * @param pGenType     the fields's generic type (NOT the same type of the field as generic)
   * @param pName        the fields's name
   * @param pAnnotations the fields's annotations
   * @param <TYPE>       the field's data type
   * @param <FIELD>      the generic field type
   * @return the newly created field
   */
  @SuppressWarnings("JavaReflectionMemberAccess")
  static <TYPE, FIELD extends IField<TYPE>> FIELD createField(Class<FIELD> pFieldType, @Nullable Class pGenType, String pName,
                                                              Collection<Annotation> pAnnotations)
  {
    try
    {
      boolean withGenType = pGenType != null;
      Constructor<FIELD> constructor = withGenType ? pFieldType.getDeclaredConstructor(Class.class, String.class, Collection.class) :
          pFieldType.getDeclaredConstructor(String.class, Collection.class);
      FIELD field = constructor.newInstance(withGenType ? new Object[]{pGenType, pName, pAnnotations} : new Object[]{pName, pAnnotations});
      _checkOptionalField(field);
      return field;
    }
    catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * Evaluates the generic type of a bean field. Returns null, if not present.
   *
   * @param pField     the field instance from the reflection API
   * @param pFieldType the type of the bean field
   * @param <FIELD>    the type of the bean field as generic
   * @return the generic type of the field instance
   */
  @Nullable
  private static <FIELD extends IField> Class _getGenType(Field pField, Class<FIELD> pFieldType)
  {
    Type genericType = pField.getGenericType();
    if (!(genericType instanceof ParameterizedType))
      return null;

    Type fieldSuperType = pFieldType.getGenericSuperclass();
    if (!(fieldSuperType instanceof ParameterizedType))
      return null;

    Type fieldSuperGenType = ((ParameterizedType) fieldSuperType).getActualTypeArguments()[0];
    return (Class) (fieldSuperGenType instanceof ParameterizedType ? ((ParameterizedType) fieldSuperGenType).getRawType() :
        ((ParameterizedType) genericType).getActualTypeArguments()[0]);
  }

  /**
   * Checks, if a bean field is marked (via {@link OptionalField}) as optional field.
   * In this case the condition, which is defined through the annotation, will be stored as an additional information in the field.
   * So this information can be used later to determine if the field is active at any moment.
   *
   * @param pField the bean field to check
   */
  private static void _checkOptionalField(IField<?> pField) throws IllegalAccessException, InstantiationException, NoSuchMethodException,
      InvocationTargetException
  {
    if (!pField.isOptional())
      return;

    OptionalField optional = pField.getAnnotation(OptionalField.class);
    assert optional != null;
    Constructor<? extends OptionalField.IActiveCondition> constructor = optional.value().getDeclaredConstructor();
    if (!constructor.isAccessible())
      constructor.setAccessible(true);
    OptionalField.IActiveCondition<?> activeCondition = constructor.newInstance();
    pField.addAdditionalInformation(OptionalField.ACTIVE_CONDITION, activeCondition);
  }
}
