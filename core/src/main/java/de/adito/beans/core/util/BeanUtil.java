package de.adito.beans.core.util;

import de.adito.beans.core.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * General utility class for the bean modell.
 *
 * @author Simon Danner, 29.06.2017
 */
public final class BeanUtil
{
  private BeanUtil()
  {
  }

  /**
   * Returns the bean as a map of fields and associated values.
   * Due to a field predicate fields may be excluded.
   *
   * @param pFieldPredicate a field predicate to determine which fields should be in the map.
   * @return a map with fields as keys and the associated bean values as values
   */
  public static Map<IField<?>, Object> asMap(IBean<?> pBean, IBeanFieldPredicate pFieldPredicate)
  {
    return pBean.stream()
        .filter(pEntry -> pFieldPredicate.test(pEntry.getKey(), pEntry.getValue()))
        //Use the LinkedHashMap-Collector to keep the order and allow null values
        .collect(LinkedHashMap::new, (pMap, pEntry) -> pMap.put(pEntry.getKey(), pEntry.getValue()), LinkedHashMap::putAll);
  }

  /**
   * Finds a field by its name.
   * This method will lead to a runtime exception if the search isn't successful.
   *
   * @param pBean      the bean where the field should exist
   * @param pFieldName the name of the searched field
   * @return the found bean field
   */
  public static IField<?> findFieldByName(IBean<?> pBean, String pFieldName)
  {
    return findFieldByName(pBean.streamFields(), pFieldName);
  }

  /**
   * Finds a field by its name.
   * This method will lead to a runtime exception if the search isn't successful.
   *
   * @param pFieldStream a stream of bean fields, which should contain the field
   * @param pFieldName   the name of the searched field
   * @return the found bean field
   */
  public static IField<?> findFieldByName(Stream<IField<?>> pFieldStream, String pFieldName)
  {
    return pFieldStream
        .filter(pField -> pField.getName().equals(pFieldName))
        .findAny()
        .orElseThrow(() -> new RuntimeException("name: " + pFieldName));
  }

  /**
   * Compares the values of two beans of some fields.
   * Both beans must contain all fields to compare, otherwise a runtime exception will be thrown.
   *
   * @param pBean1         the first bean to compare
   * @param pBean2         the second bean to compare
   * @param pFieldsToCheck a collection of fields, which should be used for the comparison
   * @return a Optional that may contain the field with a different value (it is empty if all values are equal)
   */
  public static Optional<IField> compareBeanValues(IBean pBean1, IBean pBean2, Collection<IField<?>> pFieldsToCheck)
  {
    return pFieldsToCheck.stream()
        .map(pField -> (IField) pField)
        .filter(pField -> !Objects.equals(pBean1.getValue(pField), pBean2.getValue(pField)))
        .findAny();
  }

  /**
   * Finds the equivalent bean from a collection of beans.
   * The equivalence depends on the bean fields annotated as {@link de.adito.beans.core.annotations.Identifier}.
   * The values of these fields have to be equal to fulfil this condition.
   * The bean will be removed from the collection, if the equivalent is found.
   *
   * @param pBean         the bean for which the equivalent should be found
   * @param pOldToCompare the collection of beans to compare
   * @return the equivalent bean that was removed, or null if no result
   */
  @Nullable
  public static <BEAN extends IBean<BEAN>> BEAN findRelatedBeanAndRemove(BEAN pBean, Collection<BEAN> pOldToCompare)
  {
    Iterator<BEAN> it = pOldToCompare.iterator();
    Collection<IField<?>> identifiers = pBean.getIdentifiers();
    while (it.hasNext())
    {
      BEAN oldBean = it.next();
      if (pBean.getClass() == oldBean.getClass() && //same types
          ((identifiers.isEmpty() && Objects.equals(oldBean, pBean)) || //no identifiers -> use default equals()
              (identifiers.equals(oldBean.getIdentifiers()) && !BeanUtil.compareBeanValues(oldBean, pBean, identifiers).isPresent())))
      {
        it.remove();
        return oldBean;
      }
    }

    return null;
  }

  /**
   * Resolves a deep bean within a parent bean in a hierarchical way of thinking.
   * The bean will be resolved based on a chain of bean fields, which lead the way to the deep bean.
   *
   * @param pParentBean the base parent bean
   * @param pChain      the chain of bean fields that describes the way to the deep bean
   * @return the deep bean within the parent bean
   */
  @NotNull
  public static IBean<?> resolveDeepBean(IBean<?> pParentBean, List<IField<?>> pChain)
  {
    for (IField<?> field : pChain)
    {
      if (!pParentBean.hasField(field))
        throw new RuntimeException("bad-field: " + field.getName());

      Object value = pParentBean.getValue(field);
      assert value instanceof IBean;
      pParentBean = (IBean<?>) value;
    }

    return pParentBean;
  }

  /**
   * Resolves a bean value of a deep bean field within a hierarchical structure.
   * The starting point is a parent bean, from which a chain of bean fields lead to the certain field.
   *
   * @param pParentBean the parent bean
   * @param pDeepField  the deep field to resolve the value to
   * @param pChain      the chain of bean fields that describes the way to the deep bean
   * @param <TYPE>      the data type of the deep field
   * @return the value of the deep field
   */
  @Nullable
  public static <TYPE> TYPE resolveDeepValue(IBean<?> pParentBean, IField<TYPE> pDeepField, List<IField<?>> pChain)
  {
    IBean<?> deepBean = resolveDeepBean(pParentBean, pChain);

    if (!deepBean.hasField(pDeepField))
      throw new RuntimeException("deepField: " + pDeepField.getName());

    return deepBean.getValue(pDeepField);
  }
}