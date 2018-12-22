package de.adito.ojcms.beans.util;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.fields.IField;

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
   * Checks, if a bean type is a valid declared type.
   * It has to be public and an extension of {@link Bean}.
   * Throws a runtime exception, if the type is invalid.
   * This check can be used in any cases, where especially transformed types are not allowed.
   *
   * @param pBeanType the bean type to check
   * @return the valid bean type
   */
  public static Class<? extends IBean> requiresDeclaredBeanType(Class<? extends IBean> pBeanType)
  {
    if (!Modifier.isPublic(pBeanType.getModifiers()))
      throw new RuntimeException(pBeanType.getName() + " is not a valid bean type! It has to be declared public to create fields!");

    if (!Bean.class.isAssignableFrom(pBeanType)) //To make sure it isn't a transformed type
      throw new RuntimeException(pBeanType.getName() + " is not a valid bean type! Do not use transformed bean types!");
    return pBeanType;
  }

  /**
   * Reflects the bean fields of a bean type.
   *
   * @param pBeanType the type of the bean, which must be an extension of {@link Bean} to own specific fields
   * @return a list of bean fields from the given type
   */
  private static List<IField<?>> _createBeanMetadata(Class<? extends IBean> pBeanType)
  {
    return reflectDeclaredBeanFields(requiresDeclaredBeanType(pBeanType)).stream()
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
    Class current = requiresDeclaredBeanType(pBeanType);
    final List<Field> declaredFields = new ArrayList<>();
    final Predicate<Field> combinedPredicate = pField -> Stream.of(pFieldPredicates)
        .allMatch(pPredicate -> pPredicate.test(pField));
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
