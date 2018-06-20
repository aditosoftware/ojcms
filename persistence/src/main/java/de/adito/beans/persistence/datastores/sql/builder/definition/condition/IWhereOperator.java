package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.IValueSerializer;

/**
 * An operator for a where condition.
 * This operator is able to create a condition format string based on a {@link IWhereCondition}.
 * This interface is mainly used to retrieve the different kinds of operators via static methods.
 *
 * @param <TYPE> the data type of the {@link IWhereCondition} used for this operator
 * @author Simon Danner, 06.06.2018
 */
@FunctionalInterface
public interface IWhereOperator<TYPE>
{
  /**
   * The literal for this operator the connect the column and the value.
   *
   * @return the literal
   */
  String getLiteral();

  /**
   * Converts a condition into a string format to use for SQL statements.
   * An example for the '=' operator is: "COLUMN_NAME = COLUMN_VALUE".
   *
   * @param pCondition  the condition to create the statement format
   * @param pSerializer a serializer to convert the value in a persistent string format
   * @return the condition in a statement format
   */
  default String toConditionFormat(IWhereCondition<TYPE> pCondition, IValueSerializer pSerializer)
  {
    return pCondition.getColumnDefinition().getColumnName() + " " + getLiteral() + " " + pSerializer.serialValueToStatementString(pCondition);
  }

  /**
   * The "=" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> isEqual()
  {
    return () -> "=";
  }

  /**
   * The "<>" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> isNotEqual()
  {
    return () -> "<>";
  }

  /**
   * The ">" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> greaterThan()
  {
    return () -> ">";
  }

  /**
   * The "<" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> lessThan()
  {
    return () -> "<";
  }

  /**
   * The ">=" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> greaterThanOrEqual()
  {
    return () -> ">=";
  }

  /**
   * The "<=" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> lessThanOrEqual()
  {
    return () -> "<=";
  }

  /**
   * The "BETWEEN" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> between()
  {
    return () -> "BETWEEN";
  }

  /**
   * The "LIKE" operator.
   *
   * @return a where condition operator.
   */
  static <TYPE> IWhereOperator<TYPE> like()
  {
    return () -> "LIKE";
  }
}
