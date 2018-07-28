package de.adito.beans.core.util;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Statistics;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

/**
 * A utility class for reflection of beans.
 *
 * @author Simon Danner, 23.08.2016
 */
public final class BeanReflector
{
  private static final Map<Class<? extends IBean>, List<Field>> REFLECTION_CACHE = new HashMap<>();
  private static final Map<Class<? extends IBean>, List<IField<?>>> METADATA_CACHE = new HashMap<>();

  private BeanReflector()
  {
  }

  /**
   * Reflects the bean fields of a certain bean type.
   *
   * @param pBeanType the type of the bean, which must be a sub class of {@link Bean} to own specific fields
   * @return a list of bean fields
   */
  public static List<IField<?>> reflectBeanFields(Class<? extends IBean> pBeanType)
  {
    return Collections.unmodifiableList(METADATA_CACHE.computeIfAbsent(pBeanType, BeanReflector::_createBeanMetadata));
  }

  /**
   * Reflects the declared bean fields of a certain bean type.
   *
   * @param pBeanType the type of the bean, which must be a sub class of {@link Bean} to own specific fields
   * @return a list of the declared fields
   */
  public static List<Field> reflectDeclaredBeanFields(Class<? extends IBean> pBeanType)
  {
    return Collections.unmodifiableList(REFLECTION_CACHE.computeIfAbsent(pBeanType, BeanReflector::_createDeclaredFields));
  }

  /**
   * Reflects all field statistic annotations of a bean type.
   *
   * @param pBeanType the bean's type
   * @return a map with the field names as keys and the annotations as values
   */
  @NotNull
  @SuppressWarnings("unchecked")
  public static Map<String, Statistics> getBeanStatisticAnnotations(Class<? extends IBean> pBeanType)
  {
    return Stream.of(pBeanType.getDeclaredFields())
        .filter(pField -> pField.getAnnotation(Statistics.class) != null)
        .collect(Collectors.toMap(Field::getName, pField -> pField.getAnnotation(Statistics.class)));
  }

  /**
   * Reflects the bean fields of a bean type.
   *
   * @param pBeanType the type of the bean, which must be an extension of {@link Bean} to own specific fields
   * @return a list of bean fields from the given type
   */
  private static List<IField<?>> _createBeanMetadata(Class<? extends IBean> pBeanType)
  {
    return reflectDeclaredBeanFields(_checkValidBeanType(pBeanType)).stream()
        .map(pField -> {
          try
          {
            return (IField<?>) pField.get(null);
          }
          catch (IllegalAccessException pE)
          {
            throw new RuntimeException(pE);
          }
        })
        .collect(Collectors.toList());
  }

  /**
   * Returns all public and static fields from a bean class type.
   *
   * @param pBeanType the type of the bean, which must be a sub class of {@link Bean} to own specific fields
   * @return a list of declared fields of the bean type
   */
  private static List<Field> _createDeclaredFields(Class<? extends IBean> pBeanType)
  {
    _checkValidBeanType(pBeanType);
    List<Field> declaredFields = new ArrayList<>();
    //Collect all fields, also from the superclasses
    Class current = pBeanType;
    do
    {
      declaredFields.addAll(Arrays.asList(current.getDeclaredFields()));
    }
    while ((current = current.getSuperclass()) != null && !current.equals(Bean.class));

    return declaredFields.stream()
        .filter(pField -> Modifier.isStatic(pField.getModifiers()))
        .filter(pField -> Modifier.isPublic(pField.getModifiers()))
        .filter(pField -> IField.class.isAssignableFrom(pField.getType()))
        .collect(Collectors.toList());
  }

  /**
   * Checks, if a bean type is valid for reflecting declared fields.
   * It has to be an extension of {@link Bean}.
   * Throws a runtime exception, if the type is invalid
   *
   * @param pBeanType the bean type to check
   * @return the valid bean type
   */
  private static Class<? extends IBean> _checkValidBeanType(Class<? extends IBean> pBeanType)
  {
    if (!Modifier.isPublic(pBeanType.getModifiers()))
      throw new RuntimeException(pBeanType.getName() + " is not a valid bean type! It has to be declared public to create fields!");

    if (!Bean.class.isAssignableFrom(pBeanType)) //To make sure it isn't a transformed type
      throw new RuntimeException(pBeanType.getName() + " is not a valid bean type to reflect fields from. Do not use transformed bean types!");
    return pBeanType;
  }
}
