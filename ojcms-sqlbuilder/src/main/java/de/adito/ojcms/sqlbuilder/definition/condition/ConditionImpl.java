package de.adito.ojcms.sqlbuilder.definition.condition;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;

/**
 * Basic implementation of a where condition.
 *
 * @param <VALUE> the data type of the condition's value
 * @author Simon Danner, 11.06.2018
 */
class ConditionImpl<VALUE> extends AbstractNegatable<IWhereCondition<VALUE>> implements IWhereCondition<VALUE>
{
  private final IColumnIdentification<VALUE> column;
  private final VALUE value;
  private final IWhereOperator operator;

  /**
   * Creates a new where condition.
   *
   * @param pColumn   the column identification it is based on
   * @param pValue    the value it is based on
   * @param pOperator the operator to connect the column and the value
   */
  ConditionImpl(IColumnIdentification<VALUE> pColumn, VALUE pValue, IWhereOperator pOperator)
  {
    column = pColumn;
    value = pValue;
    operator = pOperator;
  }

  @Override
  public IColumnIdentification<VALUE> getColumn()
  {
    return column;
  }

  @Override
  public VALUE getValue()
  {
    return value;
  }

  @Override
  public IWhereOperator getOperator()
  {
    return operator;
  }
}
