package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.format.IPreparedStatementFormat;

/**
 * Implementation for multiple, concatenated where conditions.
 * This class holds the added conditions and the concatenation types.
 *
 * @author Simon Danner, 09.06.2018
 */
class ConditionsImpl extends AbstractStatementConcatenation<IWhereConditions, ConditionsImpl> implements IWhereConditions
{
  /**
   * Creates a multiple where condition.
   *
   * @param pCondition the initial condition to start from
   */
  ConditionsImpl(IPreparedStatementFormat pCondition)
  {
    super(pCondition);
  }

  @Override
  public <TYPE> IWhereConditions and(IWhereCondition<TYPE> pCondition)
  {
    return addConcatenation(pCondition, EConcatenationType.AND);
  }

  @Override
  public IWhereConditions and(IWhereConditions pMultipleConditions)
  {
    return addConcatenation(pMultipleConditions, EConcatenationType.AND);
  }

  @Override
  public <TYPE> IWhereConditions or(IWhereCondition<TYPE> pCondition)
  {
    return addConcatenation(pCondition, EConcatenationType.OR);
  }

  @Override
  public IWhereConditions or(IWhereConditions pMultipleConditions)
  {
    return addConcatenation(pMultipleConditions, EConcatenationType.OR);
  }
}
