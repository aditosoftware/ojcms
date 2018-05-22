package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.modifiers.WhereModifiers;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.util.stream.*;

/**
 * An update statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Update extends AbstractSQLStatement<WhereModifiers, Void, Void, Update>
{
  private IColumnValueTuple<?>[] changes;

  /**
   * Creates a new update statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pIdColumnName      the id column name
   */
  public Update(IStatementExecutor<Void> pStatementExecutor, EDatabaseType pDatabaseType, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType, new WhereModifiers(pIdColumnName));
  }

  /**
   * Determines tuples (column + value) to update in the given table.
   *
   * @param pChanges the changes defined as column value tuples
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update set(IColumnValueTuple<?>... pChanges)
  {
    changes = pChanges;
    return this;
  }

  /**
   * Performs the update in the database.
   */
  public void update()
  {
    doQuery();
  }

  @Override
  protected Void doQuery()
  {
    if (changes.length > 0)
      executeStatement("UPDATE " + getTableName() + " SET " + _changes() + modifiers.where());
    return null;
  }

  /**
   * Enumerates the changes as one string in this format: 'COLUMN_NAME = VALUE'.
   *
   * @return the concatenated string
   */
  private String _changes()
  {
    return Stream.of(changes)
        .map(pChange -> pChange.getColumnDefinition().getColumnName().toUpperCase() + " = " + pChange.valueToStatementString())
        .collect(Collectors.joining(", "));
  }
}
