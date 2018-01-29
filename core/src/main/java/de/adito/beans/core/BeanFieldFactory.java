package de.adito.beans.core;

import de.adito.beans.core.annotations.*;
import de.adito.picoservice.IPicoRegistry;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * A static factory to create bean fields.
 * Fields should only be created via the methods of this class.
 * They take care of a lot of initialization work.
 *
 * All specific beans of the application should use this class to create their fields.
 * For an example, take a look at the {@link Bean}.
 *
 * @author Simon Danner, 23.08.2016
 */
public final class BeanFieldFactory
{
  private static final Map<Class<? extends IBean>, List<Field>> CACHE = new LinkedHashMap<>();
  private static Map<Class, Class<? extends IField>> typeFieldMapping;

  private BeanFieldFactory()
  {
  }

  /**
   * Takes a look at all static bean fields of a certain class and creates the first not initialised field automatically.
   * In this way all bean fields can be created through this method.
   * Usage: "public static final TextField name = BeanFieldFactory.create(CLASSNAME.class);"
   * This method takes care about the whole initialization of the certain field. (name, type, annotations, etc.)
   *
   * @param pBeanType the bean type to which the created field should belong to
   * @param <BEAN>    the generic type of the parameter above
   *                  (is here based on the concrete {@link Bean} class rather than on the interface. so it can not be a transformed bean type
   * @param <FIELD>   the generic type of the field that will be created
   * @return the newly created field instance
   */
  @SuppressWarnings("unchecked")
  public static <BEAN extends Bean<BEAN>, FIELD extends IField> FIELD create(Class<BEAN> pBeanType)
  {
    for (Field field : _getFields(pBeanType))
      try
      {
        if (field.get(null) == null)
        {
          Class<FIELD> fieldType = (Class<FIELD>) field.getType();
          return (FIELD) _createField(fieldType, _getGenType(field, fieldType), field.getName(), Arrays.asList(field.getAnnotations()));
        }
      }
      catch (IllegalAccessException pE)
      {
        throw new RuntimeException(pE);
      }

    throw new RuntimeException("Unable to create a bean field. There are no static fields or all of them are initialized already.");
  }

  /**
   * Provides the bean field type for a certain inner data type.
   * This depends on the field types annotated with {@link TypeDefaultField}.
   * They determine what field type is the default for the searched data type.
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
    return _createField(pFieldType, null, pName, pAnnotations);
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
  private static <TYPE, FIELD extends IField<TYPE>> FIELD _createField(Class<FIELD> pFieldType, @Nullable Class pGenType, String pName,
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
   * Returns all public, static, final fields from a bean class type.
   * They will be cached to improved performance, especially if the class has many fields to initialise.
   *
   * @param pBeanType der Typ des Beans
   * @return eine Menge von Feldern (Reflection)
   */
  private static List<Field> _getFields(Class<? extends IBean> pBeanType)
  {
    List<Field> fields = CACHE.get(pBeanType);
    if (fields == null)
    {
      fields = new ArrayList<>();
      for (Field field : pBeanType.getDeclaredFields())
      {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers) &&
            IField.class.isAssignableFrom(field.getType()))
          fields.add(field);
      }
      CACHE.put(pBeanType, fields);
    }

    return fields;
  }

  /**
   * Checks, if a bean field is marked (via {@link OptionalField}) as optional field.
   * In this case the condition, which is defined through the annotation, will be stored as additional information in the field.
   * So this information can be used later to determine if the field is active at any moment.
   *
   * @param pField the bean field to check
   */
  private static void _checkOptionalField(IField<?> pField) throws IllegalAccessException, InstantiationException
  {
    if (!pField.isOptional())
      return;

    OptionalField optional = pField.getAnnotation(OptionalField.class);
    assert optional != null;
    OptionalField.IActiveCondition<?> activeCondition = optional.value().newInstance();
    pField.addAdditionalInformation(OptionalField.ACTIVE_CONDITION, activeCondition);
  }
}
