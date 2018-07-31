package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

/**
 * Implementation for multiple, concatenated where conditions for id columns.
 * This class holds the added conditions and the concatenation types.
 * It stores {@link OperatorWithId} objects as conditions. They provide a where condition statement format later on.
 *
 * @author Simon Danner, 21.07.2018
 */
class IdConditionsImpl extends AbstractStatementConcatenation<IWhereConditionsForId, IdConditionsImpl> implements IWhereConditionsForId
{
  /**
   * Creates a multiple id column condition.
   *
   * @param pOperator the operator for the first condition
   * @param pId       the id value for the first condition
   */
  IdConditionsImpl(IWhereOperator pOperator, int pId)
  {
    super(new OperatorWithId(pOperator, pId));
  }

  /**
   * Creates a multiple id column condition.
   *
   * @param pMultipleConditions another multiple condition to start from
   */
  IdConditionsImpl(IWhereConditionsForId pMultipleConditions)
  {
    super(pMultipleConditions);
  }

  @Override
  public IWhereConditionsForId and(IWhereOperator pOperator, int pId)
  {
    return addConcatenation(new OperatorWithId(pOperator, pId), EConcatenationType.AND);
  }

  @Override
  public IWhereConditionsForId andNot(IWhereOperator pOperator, int pId)
  {
    return addConcatenation(new OperatorWithId(pOperator, pId).not(), EConcatenationType.AND);
  }

  @Override
  public IWhereConditionsForId and(IWhereConditionsForId pMultipleConditions)
  {
    return addConcatenation(pMultipleConditions, EConcatenationType.AND);
  }

  @Override
  public IWhereConditionsForId or(IWhereOperator pOperator, int pId)
  {
    return addConcatenation(new OperatorWithId(pOperator, pId), EConcatenationType.OR);
  }

  @Override
  public IWhereConditionsForId orNot(IWhereOperator pOperator, int pId)
  {
    return addConcatenation(new OperatorWithId(pOperator, pId).not(), EConcatenationType.OR);
  }

  @Override
  public IWhereConditionsForId or(IWhereConditionsForId pMultipleConditions)
  {
    return addConcatenation(pMultipleConditions, EConcatenationType.OR);
  }
}
