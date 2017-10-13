package de.adito.beans.core;

import de.adito.beans.core.annotations.OptionalField;
import de.adito.beans.core.annotations.TypeDefaultField;
import de.adito.picoservice.IPicoRegistry;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * Statische Factory für Bean-Felder.
 * Die Methode create(pType) soll zur Erzeugung jedes Bean-Feldes verwendet werden.
 *
 * @author s.danner, 23.08.2016
 */
public final class BeanFieldFactory
{
  private static final Map<Class<? extends IBean>, List<Field>> CACHE = new LinkedHashMap<>();
  private static Map<Class, Class<? extends IField>> typeFieldMapping;

  private BeanFieldFactory()
  {
  }

  /**
   * Erzeugt über Reflection das erste Bean-Feld, welches in der übergebenen Bean-Klasse noch nicht initialisiert wurde.
   * Dabei wird das Feld mit den initialen Daten wie Typ, Name und Annotations belegt.
   * Verwendung: public static final TextField name = BeanFieldFactory.create(CLASSNAME.class);
   * Wenn diese Methode auf diese Weise angewandt wird, wird immer das richtige Feld zurückgegeben.
   *
   * @param pBeanType der Klassen-Typ der Bean, wozu das Feld erzeugt werden soll
   * @param <BEAN>    der generische Typ der Bean, auf welchen sich der Bean-Typ-Parameter bezieht
   *                  (Muss hier auf der Klasse Bean basieren! -> da es sich um ein echtes Bean-Modell handeln soll und nicht um eine transformierte)
   * @param <FIELD>   der Typ des Feldes, welches erzeugt wird
   * @return die zum statistischen Feld gehörende Bean-Feld-Instanz
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

    throw new RuntimeException();
  }

  /**
   * Liefert den Bean-Feld-Typen aufgrund des beinhaltenden Daten-Typs
   *
   * @param pType  der Daten-Typ
   * @param <TYPE> der Daten Typ als Generic
   * @return der Typ des Bean-Feldes, welcher zum Datentyp passt
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
                                      throw new RuntimeException(pFieldType1.getSimpleName() + " supports same datatype as "
                                          + pFieldType2.getSimpleName());
                                    }));

    if (!typeFieldMapping.containsKey(pType))
      throw new RuntimeException("type: " + pType.getSimpleName());
    //noinspection unchecked
    return (Class<IField<TYPE>>) typeFieldMapping.get(pType);
  }

  /**
   * Erzeugt ein neues Bean-Feld anhand des Typen und den initialen Daten.
   *
   * @param pFieldType   der Bean-Feld-Typ
   * @param pName        der Name des Feldes
   * @param pAnnotations die Annotations des Feldes
   * @param <TYPE>       der Daten-Typ des Bean-Feldes
   * @param <FIELD>      der Bean-Feld-Typ als Generic
   * @return das neu erzeugte Feld
   */
  static <TYPE, FIELD extends IField<TYPE>> FIELD createField(Class<FIELD> pFieldType, String pName, Collection<Annotation> pAnnotations)
  {
    return _createField(pFieldType, null, pName, pAnnotations);
  }

  /**
   * Erzeugt ein neues Bean-Feld anhand des Typen und den initialen Daten.
   * Zusätzlich kann hier ein generischer Typ zum Bean-Feld mitgeliefert werden.
   *
   * @param pFieldType   der Bean-Feld-Typ
   * @param pGenType     der generische Typ des Bean-Feldes
   * @param pName        der Name des Feldes
   * @param pAnnotations die Annotations des Feldes
   * @param <TYPE>       der Daten-Typ des Bean-Feldes
   * @param <FIELD>      der Bean-Feld-Typ als Generic
   * @return das neu erzeugte Feld
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
   * Liefert den generischen Typen eines Bean-Feldes, wenn dieser vorhanden ist, sonst null.
   *
   * @param pField     das Feld auf Klassenebene
   * @param pFieldType der Bean-Feld-Typ
   * @param <FIELD>    der generische Typ des Bean-Feldes
   * @return der generische Typ des Bean-Feldes
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
   * Liefert die Felder eines Bean-Typen.
   * Dabei wird ein Caching vorgenommen.
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
   * Überprüft, ob es sich bei einem Feld um ein optionales handelt.
   * Wenn dies der Fall ist, wird dem Feld eine zusätzliche Information angefügt,
   * welche bestimmt, ob das Feld gerade aktiv ist.
   *
   * @param pField das betreffende Bean-Feld
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
