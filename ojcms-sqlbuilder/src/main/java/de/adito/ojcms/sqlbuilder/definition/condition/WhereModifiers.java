package de.adito.ojcms.sqlbuilder.definition.condition;

import de.adito.ojcms.sqlbuilder.format.IPreparedStatementFormat;

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
   * The optional where id condition.
   *
   * @return an optional (maybe concatenated) where id condition
   */
  public Optional<IWhereConditionsForId> getWhereIdCondition()
  {
    return Optional.ofNullable(idCondition);
  }

  /**
   * The optional where condition.
   *
   * @return an optional (maybe concatenated) where condition
   */
  public Optional<IWhereConditions> getWhereCondition()
  {
    return Optional.ofNullable(whereCondition);
  }

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
    final IPreparedStatementFormat condition;
    if (idCondition == null)
      condition = whereCondition;
    else if (whereCondition == null)
      condition = idCondition;
    else
      condition = new _Combiner();
    return Optional.of(condition);
  }

  /**
   * Combines the id condition and the where condition.
   */
  private class _Combiner extends AbstractStatementConcatenation<_Combiner, _Combiner>
  {
    private _Combiner()
    {
      super(idCondition);
      addConcatenation(Objects.requireNonNull(whereCondition), EConcatenationType.AND);
    }
  }
}
