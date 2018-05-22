package de.adito.beans.persistence.datastores.sql.builder;

import java.io.Closeable;

/**
 * An executor for SQL statements.
 * The execution may lead to a result.
 * This executor is closeable to detach the connection to the database afterwards.
 *
 * @author Simon Danner, 28.04.2018
 */
public interface IStatementExecutor<RESULT> extends Closeable
{
  /**
   * Executes a SQL statement.
   *
   * @param pSQLStatement the statement to execute
   * @return the result of the execution
   */
  RESULT executeStatement(String pSQLStatement);
}
