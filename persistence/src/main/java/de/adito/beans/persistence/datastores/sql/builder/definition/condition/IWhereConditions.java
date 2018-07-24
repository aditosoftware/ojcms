package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import java.util.List;
import java.util.stream.*;

/**
 * Multiple, concatenated where conditions.
 * Single conditions can be added to this combined condition.
 * This type is iterable for the single contained conditions (single or multiple).
 *
 * @author Simon Danner, 09.06.2018
 */
public interface IWhereConditions extends IMultipleCondition<IWhereConditions>
{
  /**
   * Creates the multiple condition holder starting with a single condition.
   *
   * @param pCondition the where condition to start from
   * @param <TYPE>     the data type of the condition
   * @return the multiple condition holder
   */
  static <TYPE> IWhereConditions create(IWhereCondition<TYPE> pCondition)
  {
    return new ConditionsImpl(pCondition);
  }

  /**
   * Creates the multiple condition holder starting with another concatenated condition.
   *
   * @param pMultipleConditions the concatenated condition to start from
   * @return the multiple condition holder
   */
  static IWhereConditions create(IWhereConditions pMultipleConditions)
  {
    return new ConditionsImpl(pMultipleConditions);
  }

  /**
   * Creates the multiple condition from a stream of conditions.
   * It it only possible to combine all conditions per "AND" or "OR".
   *
   * @param pConditions        the stream of conditions to concatenate
   * @param pConcatenationType the concatenation type
   * @return the multiple condition holder
   */
  static IWhereConditions createFromMultiple(Stream<IWhereCondition<?>> pConditions, EConcatenationType pConcatenationType)
  {
    List<IWhereCondition<?>> conditionList = pConditions.collect(Collectors.toList());
    IWhereConditions conditions = IWhereConditions.create(conditionList.remove(0));
    conditionList.forEach(pCondition -> {
      if (pConcatenationType == EConcatenationType.AND)
        conditions.and(pCondition);
      else
        conditions.or(pCondition);
    });
    return conditions;
  }

  /**
   * Adds a single where condition with a "AND" concatenation type for the last entry.
   *
   * @param pCondition the single where condition to add
   * @param <TYPE>     the data type of the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  <TYPE> IWhereConditions and(IWhereCondition<TYPE> pCondition);

  /**
   * Adds a single where condition with a "AND NOT" concatenation type for the last entry.
   *
   * @param pCondition the single where condition to add
   * @param <TYPE>     the data type of the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default <TYPE> IWhereConditions andNot(IWhereCondition<TYPE> pCondition)
  {
    return and(pCondition.not());
  }

  /**
   * Adds another concatenated condition with a "AND" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditions and(IWhereConditions pMultipleConditions);

  /**
   * Adds another concatenated condition with a "AND NOT" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default IWhereConditions andNot(IWhereConditions pMultipleConditions)
  {
    return and(pMultipleConditions.not());
  }

  /**
   * Adds a single where condition with a "OR" concatenation type for the last entry.
   *
   * @param pCondition the single where condition to add
   * @param <TYPE>     the data type of the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  <TYPE> IWhereConditions or(IWhereCondition<TYPE> pCondition);

  /**
   * Adds a single where condition with a "OR NOT" concatenation type for the last entry.
   *
   * @param pCondition the single where condition to add
   * @param <TYPE>     the data type of the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default <TYPE> IWhereConditions orNot(IWhereCondition<TYPE> pCondition)
  {
    return or(pCondition.not());
  }

  /**
   * Adds another concatenated condition with a "OR" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditions or(IWhereConditions pMultipleConditions);

  /**
   * Adds another concatenated condition with a "OR NOT" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default IWhereConditions orNot(IWhereConditions pMultipleConditions)
  {
    return or(pMultipleConditions.not());
  }
}
