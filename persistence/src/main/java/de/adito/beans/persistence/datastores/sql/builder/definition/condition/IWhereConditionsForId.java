package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

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
  static IWhereConditionsForId create(IWhereOperator pOperator, int pId)
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
   * Adds a single id condition with a "AND" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId and(IWhereOperator pOperator, int pId);

  /**
   * Adds a single id condition with a "AND NOT" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId andNot(IWhereOperator pOperator, int pId);

  /**
   * Adds another concatenated condition with a "AND" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId and(IWhereConditionsForId pMultipleConditions);

  /**
   * Adds another concatenated condition with a "AND NOT" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default IWhereConditionsForId andNot(IWhereConditionsForId pMultipleConditions)
  {
    return and(pMultipleConditions.not());
  }

  /**
   * Adds a single id condition with a "OR" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId or(IWhereOperator pOperator, int pId);

  /**
   * Adds a single where condition with a "OR NOT" concatenation type for the last entry.
   *
   * @param pOperator the operator for the id condition
   * @param pId       the id value for the condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId orNot(IWhereOperator pOperator, int pId);

  /**
   * Adds another concatenated condition with a "OR" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  IWhereConditionsForId or(IWhereConditionsForId pMultipleConditions);

  /**
   * Adds another concatenated condition with a "OR NOT" concatenation type for the last entry.
   *
   * @param pMultipleConditions the concatenated condition to add
   * @return the concatenated condition itself for a pipelining mechanism
   */
  default IWhereConditionsForId orNot(IWhereConditionsForId pMultipleConditions)
  {
    return or(pMultipleConditions.not());
  }
}
