package de.adito.ojcms.sqlbuilder.definition.condition;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;

import java.util.Set;

/**
 * Multiple, concatenated where conditions for the id column.
 * Single id conditions can be added to this combined condition.
 * This type is iterable for the single contained conditions (single or multiple).
 *
 * @author Simon Danner, 09.06.2018
 */
public interface IWhereConditionsForId extends IMultipleCondition<IWhereConditionsForId>
{
  /**
   * Creates the multiple condition holder starting with a single id condition.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the multiple id condition holder
   */
  static IWhereConditionsForId create(IWhereOperator pOperator, long pId)
  {
    return new IdConditionsImpl(pOperator, pId);
  }

  /**
   * Creates the multiple condition holder starting with another concatenated id condition.
   *
   * @param pMultipleConditions the concatenated id condition to start from
   * @return the multiple id condition holder
   */
  static IWhereConditionsForId create(IWhereConditionsForId pMultipleConditions)
  {
    return new IdConditionsImpl(pMultipleConditions);
  }

  /**
   * Creates the multiple condition holder starting with an in() id condition.
   *
   * @param pIds multiple ids for the in condition
   * @return the multiple id condition holder
   */
  static IWhereConditionsForId in(Set<Long> pIds, IColumnIdentification<Long> pIdColumnIdentification)
  {
    return new IdConditionsImpl(pIds, pIdColumnIdentification);
  }

  /**
   * Adds a single id condition with an "AND" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId and(IWhereOperator pOperator, long pId);

  /**
   * Adds a single id condition with an "AND NOT" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId andNot(IWhereOperator pOperator, long pId);

  /**
   * Adds another concatenated condition with an "AND" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId and(IWhereConditionsForId pMultipleConditions);

  /**
   * Adds another concatenated condition with an "AND NOT" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default IWhereConditionsForId andNot(IWhereConditionsForId pMultipleConditions)
  {
    return and(pMultipleConditions.not());
  }

  /**
   * Adds a single id condition with an "OR" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId or(IWhereOperator pOperator, long pId);

  /**
   * Adds a single where condition with an "OR NOT" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId orNot(IWhereOperator pOperator, long pId);

  /**
   * Adds another concatenated condition with an "OR" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId or(IWhereConditionsForId pMultipleConditions);

  /**
   * Adds another concatenated condition with an "OR NOT" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default IWhereConditionsForId orNot(IWhereConditionsForId pMultipleConditions)
  {
    return or(pMultipleConditions.not());
  }
}
