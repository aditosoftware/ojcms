package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.format.IPreparedStatementFormat;

import java.util.*;

/**
 * Condition based modifiers for SQL statements.
 *
 * @author Simon Danner, 26.04.2018
 */
public class WhereModifiers
{
  private IWhereConditionsForId idCondition;
  private IWhereConditions whereCondition;

  /**
   * Sets a multiple where condition for the id column.
   *
   * @param pMultipleIdCondition the multiple id condition
   */
  public void setWhereIdCondition(IWhereConditionsForId pMultipleIdCondition)
  {
    idCondition = pMultipleIdCondition;
  }

  /**
   * Sets the where condition.
   *
   * @param pCondition the where condition for the statement
   */
  public void setWhereCondition(IWhereConditions pCondition)
  {
    whereCondition = pCondition;
  }

  /**
   * The where condition statement format.
   *
   * @return an optional statement format for the where condition
   */
  public Optional<IPreparedStatementFormat> where()
  {
    if (idCondition == null && whereCondition == null)
      return Optional.empty();
    return Optional.of(idCondition == null ? whereCondition : whereCondition == null ? idCondition : new _Combiner());
  }

  /**
   * Combines the id condition and the where condition.
   */
  private class _Combiner extends AbstractStatementConcatenation<_Combiner, _Combiner>
  {
    private _Combiner()
    {
      super(Objects.requireNonNull(idCondition));
      addConcatenation(Objects.requireNonNull(whereCondition), EConcatenationType.AND);
    }
  }
}
