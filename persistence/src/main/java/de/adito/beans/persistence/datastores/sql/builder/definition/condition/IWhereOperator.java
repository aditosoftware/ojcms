package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

/**
 * An operator for a where condition.
 * This interface is mainly used to retrieve the different kinds of operators via static methods.
 *
 * @author Simon Danner, 06.06.2018
 */
@FunctionalInterface
public interface IWhereOperator
{
  /**
   * The literal for this operator the connect the column and the value.
   *
   * @return the literal
   */
  String getLiteral();

  /**
   * The "=" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator isEqual()
  {
    return () -> "=";
  }

  /**
   * The "<>" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator isNotEqual()
  {
    return () -> "<>";
  }

  /**
   * The ">" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator greaterThan()
  {
    return () -> ">";
  }

  /**
   * The "<" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator lessThan()
  {
    return () -> "<";
  }

  /**
   * The ">=" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator greaterThanOrEqual()
  {
    return () -> ">=";
  }

  /**
   * The "<=" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator lessThanOrEqual()
  {
    return () -> "<=";
  }

  /**
   * The "BETWEEN" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator between()
  {
    return () -> "BETWEEN";
  }

  /**
   * The "LIKE" operator.
   *
   * @return a where condition operator.
   */
  static IWhereOperator like()
  {
    return () -> "LIKE";
  }
}
