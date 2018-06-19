package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.IColumnDefinition;

/**
 * Basic implementation of a where condition.
 *
 * @param <TYPE> the data type of the condition's value
 * @author Simon Danner, 11.06.2018
 */
class ConditionImpl<TYPE> extends AbstractNegatable<IWhereCondition<TYPE>> implements IWhereCondition<TYPE>
{
  private final IColumnDefinition<TYPE> column;
  private final TYPE value;
  private final IWhereOperator<TYPE> operator;

  /**
   * Creates a new where condition.
   *
   * @param pColumn   the column definition it is based on
   * @param pValue    the value it is based on
   * @param pOperator the operator to connect the column and the value
   */
  ConditionImpl(IColumnDefinition<TYPE> pColumn, TYPE pValue, IWhereOperator<TYPE> pOperator)
  {
    column = pColumn;
    value = pValue;
    operator = pOperator;
  }

  @Override
  public IColumnDefinition<TYPE> getColumnDefinition()
  {
    return column;
  }

  @Override
  public TYPE getValue()
  {
    return value;
  }

  @Override
  public IWhereOperator<TYPE> getOperator()
  {
    return operator;
  }
}
