package de.adito.beans.persistence.datastores.sql.builder;

import java.io.Closeable;
import java.util.*;

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
   * @param pArgs         arguments for prepared statements
   * @return the result of the execution
   */
  RESULT executeStatement(String pSQLStatement, List<String> pArgs);

  /**
   * Executes a SQL statement.
   *
   * @param pSQLStatement the statement to execute
   * @param pArgs         arguments for prepared statements
   * @return the result of the execution
   */
  default RESULT executeStatement(String pSQLStatement, String... pArgs)
  {
    return executeStatement(pSQLStatement, Arrays.asList(pArgs));
  }
}
