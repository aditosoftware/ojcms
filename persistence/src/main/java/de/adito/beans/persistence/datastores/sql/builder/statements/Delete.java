package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.modifiers.WhereModifiers;
import de.adito.beans.persistence.datastores.sql.builder.util.EDatabaseType;

/**
 * A delete statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Delete extends AbstractSQLStatement<WhereModifiers, Boolean, Boolean, Delete>
{
  /**
   * Creates a new delete statement.
   *
   * @param pStatementExecutor the executor fot this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pIdColumnName      the name of the id column
   */
  public Delete(IStatementExecutor<Boolean> pStatementExecutor, EDatabaseType pDatabaseType, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType, new WhereModifiers(pIdColumnName));
  }

  /**
   * Performs the deletion.
   *
   * @return <tt>true</tt>, if the deletion was successful
   */
  public boolean delete()
  {
    return doQuery();
  }

  @Override
  protected Boolean doQuery()
  {
    return executeStatement("DELETE FROM " + getTableName() + modifiers.where());
  }
}
