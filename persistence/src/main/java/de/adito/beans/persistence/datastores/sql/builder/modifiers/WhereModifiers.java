package de.adito.beans.persistence.datastores.sql.builder.modifiers;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.*;

import java.util.Optional;

/**
 * Condition based modifiers for SQL statements.
 *
 * @author Simon Danner, 26.04.2018
 */
public class WhereModifiers
{
  private final IValueSerializer serializer;
  private final IColumnIdentification<Integer> idColumnIdentification;
  private IWhereCondition<Integer> idCondition;
  private IWhereConditions whereCondition;

  /**
   * Creates new where modifiers.
   *
   * @param pSerializer   a value serializer
   * @param pIdColumnName the global name of the id column
   */
  public WhereModifiers(IValueSerializer pSerializer, String pIdColumnName)
  {
    serializer = pSerializer;
    idColumnIdentification = IColumnIdentification.of(pIdColumnName, Integer.class);
  }

  /**
   * The where id of this modifiers.
   * Might not be set.
   *
   * @return an optional where id
   */
  public Optional<Integer> getWhereId()
  {
    return idCondition == null ? Optional.empty() : Optional.of(idCondition.getValue());
  }

  /**
   * Sets an id for the condition to only affect rows with a certain id.
   *
   * @param pId the id for the condition
   */
  public void setWhereId(int pId)
  {
    idCondition = IWhereCondition.isEqual(idColumnIdentification, pId);
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
   * Builds the where condition string based on the given values.
   *
   * @return a string that could be used in a conditional SQL statement
   */
  public String where()
  {
    if (idCondition == null && whereCondition == null)
      return "";

    String where = " WHERE ";

    if (idCondition == null)
      where += whereCondition.toStatementFormat(serializer);
    else if (whereCondition == null)
      where += idCondition.toStatementFormat(serializer);
    else
      where += IWhereConditions.create(idCondition)
          .and(whereCondition)
          .toStatementFormat(serializer);

    return where;
  }
}
