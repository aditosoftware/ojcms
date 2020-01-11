package de.adito.ojcms.sqlbuilder.executors;

import de.adito.ojcms.sqlbuilder.AbstractSQLStatement;
import de.adito.ojcms.sqlbuilder.serialization.ISerialValue;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.sql.*;
import java.util.function.*;

/**
 * Provides functionality to execute string based SQL statements or {@link AbstractSQLStatement} instances.
 *
 * @author Simon Danner, 05.01.2020
 */
public final class StatementExecution
{
  private final Supplier<Connection> connectionSupplier;
  private final boolean closeConnectionAfterStatement;

  /**
   * Initializes the SQL executor with a supplier for {@link Connection}.
   * The SQL executors either create new connections for every statement or use a single one for every statement.
   *
   * @param pConnectionSupplier            supplier for database connections
   * @param pCloseConnectionAfterStatement <tt>true</tt> if the database connection should be closed after every statement
   */
  public StatementExecution(Supplier<Connection> pConnectionSupplier, boolean pCloseConnectionAfterStatement)
  {
    connectionSupplier = pConnectionSupplier;
    closeConnectionAfterStatement = pCloseConnectionAfterStatement;
  }

  /**
   * Performs a no result database statement and closes the connection afterwards, if necessary.
   *
   * @param pStatement         the initial statement to configure and to execute afterwards
   * @param pStatementConsumer a consumer of the specific statement to configure it before the execution
   * @param <STATEMENT>        the type of the specific statement to execute
   */
  public <STATEMENT extends AbstractSQLStatement<Void, STATEMENT>> void execute(STATEMENT pStatement, Consumer<STATEMENT> pStatementConsumer)
  {
    pStatementConsumer.accept(pStatement);
    _tryClose(pStatement);
  }

  /**
   * Performs a database query for a specific result and closes the connection afterwards, if necessary.
   *
   * @param pQuery         the initial query to configure and to execute afterwards
   * @param pQueryConsumer a function that will be provided with the query to configure it before the execution
   * @param <RESULT>       the type of the result of this query
   * @param <INNERRESULT>  the type of the result of the specific {@link IStatementExecutor} for this query
   * @param <QUERY>        the type of the specific query
   * @return the result determined by the given query
   */
  public <RESULT, INNERRESULT, QUERY extends AbstractSQLStatement<INNERRESULT, QUERY>> RESULT query(QUERY pQuery, Function<QUERY, RESULT> pQueryConsumer)
  {
    try
    {
      return pQueryConsumer.apply(pQuery);
    }
    finally
    {
      _tryClose(pQuery);
    }
  }

  /**
   * Executes a SQL statement with no result.
   * If necessary, the connection will be closed after the execution.
   *
   * @param pSQLStatement the SQL statement to execute
   * @param pArgs         arguments for the prepared statement
   */
  public void executeVoidStatement(String pSQLStatement, ISerialValue... pArgs)
  {
    final IStatementExecutor<Void> executor = createVoidExecutor();
    executor.executeStatement(pSQLStatement, pArgs);
    _tryClose(executor);
  }

  /**
   * Retrieves any information of {@link DatabaseMetaData}.
   * The connection will be closed afterwards. Do not keep a reference to the metadata instance.
   *
   * @param pResultResolver a function to retrieve the information from the database metadata
   * @param <RESULT>        the type of the information/result
   * @return the information from the database metadata
   */
  public <RESULT> RESULT retrieveFromMetaData(ThrowingFunction<DatabaseMetaData, RESULT, SQLException> pResultResolver)
  {
    final Connection connection = connectionSupplier.get();
    try
    {
      return pResultResolver.apply(connection.getMetaData());
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
    finally
    {
      _tryClose(connection);
    }
  }

  /**
   * Determines if the database connection will be closed after every statement.
   *
   * @return <tt>true</tt> if the connections will be closed
   */
  public boolean isConnectionClosedAfterExecution()
  {
    return closeConnectionAfterStatement;
  }

  /**
   * Creates a {@link IStatementExecutor} that provides a {@link ResultSet} on completion.
   *
   * @return the created statement executor
   */
  public IStatementExecutor<ResultSet> createExecutor()
  {
    return new StatementExecutor<>(connectionSupplier, PreparedStatement::executeQuery);
  }

  /**
   * Creates a {@link IStatementExecutor} for statement that do not provide any result.
   *
   * @return the created statement executor
   */
  public IStatementExecutor<Void> createVoidExecutor()
  {
    return new StatementExecutor<>(connectionSupplier, pStatement -> {
      pStatement.execute();
      return null;
    });
  }

  /**
   * Creates a {@link IStatementExecutor} that will return if the execution has been successful.
   *
   * @return the created statement executor
   */
  public IStatementExecutor<Boolean> createSuccessExecutor()
  {
    return new StatementExecutor<>(connectionSupplier, pStatement -> {
      try
      {
        pStatement.execute();
        return true;
      }
      catch (SQLException pE)
      {
        return false;
      }
    });
  }

  /**
   * Closes an {@link AutoCloseable} if this executor instance is configured to close after every statement.
   *
   * @param pCloseable the closeable, that represents a connection
   */
  private void _tryClose(AutoCloseable pCloseable)
  {
    try
    {
      if (closeConnectionAfterStatement)
        pCloseable.close();
    }
    catch (Exception pE)
    {
      throw new OJDatabaseException(pE, "Unable to close the database connection!");
    }
  }
}
