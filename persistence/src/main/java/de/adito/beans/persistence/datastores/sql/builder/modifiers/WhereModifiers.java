package de.adito.beans.persistence.datastores.sql.builder.modifiers;

import de.adito.beans.persistence.datastores.sql.builder.util.IColumnValueTuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Condition based modifiers for SQL statements.
 *
 * @author Simon Danner, 26.04.2018
 */
public class WhereModifiers
{
  protected int id = -1;
  protected final Set<IColumnValueTuple<?>> whereConditions = new HashSet<>();
  private final String idColumnName;

  public WhereModifiers(String pIdColumnName)
  {
    idColumnName = pIdColumnName;
  }

  /**
   * Sets an id for a condition based on an id-column.
   *
   * @param pId the id for the condition
   */
  public void setId(int pId)
  {
    id = pId;
  }

  /**
   * Sets a variable amount of where conditions for a SQL statement.
   * The conditions will be created from column value tuples ('COLUMN_NAME = VALUE').
   *
   * @param pConditions the conditions for the statement
   */
  public void setWhereConditions(IColumnValueTuple<?>... pConditions)
  {
    whereConditions.addAll(Arrays.asList(pConditions));
  }

  /**
   * Builds the where condition string based on the given values.
   *
   * @return a string that could be used in a conditional SQL statement
   */
  public String where()
  {
    if (id == -1 && whereConditions.isEmpty())
      return "";

    return " WHERE " + (id != -1 ? idColumnName + " = " + id + " " : "") +
        whereConditions.stream()
            .map(pCondition -> pCondition.getColumnDefinition().getColumnName().toUpperCase() + " = " + pCondition.valueToStatementString())
            .collect(Collectors.joining(" AND "));
  }
}
