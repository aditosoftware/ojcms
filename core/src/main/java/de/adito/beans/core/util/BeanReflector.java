package de.adito.beans.core.util;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Statistics;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

/**
 * A utility class for reflection of beans.
 *
 * @author Simon Danner, 23.08.2016
 */
public final class BeanReflector
{
  private static final Map<Class<? extends IBean>, List<Field>> REFLECTION_BEAN_CACHE = new HashMap<>();
  private static final Map<Class<? extends IBean>, List<Field>> REFLECTION_NON_BEAN_CACHE = new HashMap<>();
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
    return Collections.unmodifiableList(REFLECTION_BEAN_CACHE.computeIfAbsent(pBeanType, BeanReflector::_getDeclaredBeanFields));
  }

  /**
   * Reflects the declared non bean fields of a certain bean type.
   *
   * @param pBeanType the type of the bean to find the non bean fields
   * @return a list of the declared fields
   */
  public static List<Field> reflectDeclaredNonBeanFields(Class<? extends IBean> pBeanType)
  {
    return Collections.unmodifiableList(REFLECTION_NON_BEAN_CACHE.computeIfAbsent(pBeanType, BeanReflector::_getDeclaredNonBeanFields));
  }

  /**
   * Reflects all field statistic annotations of a bean type.
   *
   * @param pBeanType the bean's type
   * @return a map with the field names as keys and the annotations as values
   */
  @NotNull
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
    return reflectDeclaredBeanFields(BeanUtil.requiresDeclaredBeanType(pBeanType)).stream()
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
   * Returns all public and static bean fields from a bean class type.
   *
   * @param pBeanType the type of the bean, which must be a sub class of {@link Bean} to own specific fields
   * @return a list of declared fields of the bean type
   */
  private static List<Field> _getDeclaredBeanFields(Class<? extends IBean> pBeanType)
  {
    return _getDeclaredFields(pBeanType,
                              pField -> Modifier.isStatic(pField.getModifiers()),
                              pField -> Modifier.isPublic(pField.getModifiers()),
                              pField -> IField.class.isAssignableFrom(pField.getType()));
  }

  /**
   * Returns all non bean fields from a bean type.
   * The access modifier is irrelevant for this method.
   *
   * @param pBeanType the type of the bean to find the non bean fields
   * @return a list of declared fields
   */
  private static List<Field> _getDeclaredNonBeanFields(Class<? extends IBean> pBeanType)
  {
    return _getDeclaredFields(pBeanType,
                              pField -> !Modifier.isStatic(pField.getModifiers()),
                              pField -> !IField.class.isAssignableFrom(pField.getType()));
  }

  /**
   * All declared fields from a certain bean type, which apply to a variable amount of predicates.
   *
   * @param pBeanType        the bean type to find the fields
   * @param pFieldPredicates the field predicates, that determine which fields should be collected
   * @return a list of declared fields
   */
  @SafeVarargs
  private static List<Field> _getDeclaredFields(Class<? extends IBean> pBeanType, Predicate<Field>... pFieldPredicates)
  {
    BeanUtil.requiresDeclaredBeanType(pBeanType);
    final List<Field> declaredFields = new ArrayList<>();
    final Predicate<Field> combinedPredicate = pField -> Stream.of(pFieldPredicates)
        .allMatch(pPredicate -> pPredicate.test(pField));
    Class current = pBeanType;
    do
    {
      Stream.of(current.getDeclaredFields())
          .filter(combinedPredicate)
          .forEach(declaredFields::add);
    }
    while ((current = current.getSuperclass()) != null && !current.equals(Bean.class));
    return declaredFields;
  }
}
