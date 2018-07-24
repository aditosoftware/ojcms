package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.IColumnIdentification;

/**
 * Basic implementation of a where condition.
 *
 * @param <TYPE> the data type of the condition's value
 * @author Simon Danner, 11.06.2018
 */
class ConditionImpl<TYPE> extends AbstractNegatable<IWhereCondition<TYPE>> implements IWhereCondition<TYPE>
{
  private final IColumnIdentification<TYPE> column;
  private final TYPE value;
  private final IWhereOperator operator;

  /**
   * Creates a new where condition.
   *
   * @param pColumn   the column identification it is based on
   * @param pValue    the value it is based on
   * @param pOperator the operator to connect the column and the value
   */
  ConditionImpl(IColumnIdentification<TYPE> pColumn, TYPE pValue, IWhereOperator pOperator)
  {
    column = pColumn;
    value = pValue;
    operator = pOperator;
  }

  @Override
  public IColumnIdentification<TYPE> getColumn()
  {
    return column;
  }

  @Override
  public TYPE getValue()
  {
    return value;
  }

  @Override
  public IWhereOperator getOperator()
  {
    return operator;
  }
}
