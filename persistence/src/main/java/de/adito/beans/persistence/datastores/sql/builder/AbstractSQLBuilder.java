package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.statements.*;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;
import java.util.function.*;

/**
 * Abstract base class for the SQL builder.
 * The database statements can be used and adapted in an functional way like the Java streams.
 *
 * @author Simon Danner, 19.02.2018
 */
abstract class AbstractSQLBuilder
{
  private final EDatabaseType databaseType;
  private final Supplier<Connection> connectionSupplier;
  private final boolean closeAfterStatement;
  private final IValueSerializer serializer;
  private final String idColumnName; //global id column name for this builder

  /**
   * Creates a new builder.
   *
   * @param pDatabaseType        the database type to use for this builder
   * @param pConnectionSupplier  the database connection supplier
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pIdColumnName        a global id column name for this builder instance
   */
  protected AbstractSQLBuilder(EDatabaseType pDatabaseType, Supplier<Connection> pConnectionSupplier, boolean pCloseAfterStatement,
                               IValueSerializer pSerializer, String pIdColumnName)
  {
    if (pIdColumnName == null || pIdColumnName.isEmpty())
      throw new IllegalArgumentException("the id column name must given! name: " + pIdColumnName);
    databaseType = Objects.requireNonNull(pDatabaseType);
    connectionSupplier = Objects.requireNonNull(pConnectionSupplier);
    closeAfterStatement = pCloseAfterStatement;
    serializer = Objects.requireNonNull(pSerializer);
    idColumnName = pIdColumnName;
  }

  /**
   * Executes a create statement.
   *
   * @param pCreateStatement the statement to execute (defined in a pipelining mechanism)
   */
  public void doCreate(Consumer<Create> pCreateStatement)
  {
    _execute(configureStatementBeforeExecution(new Create(_createNoResultExecutor(), databaseType, serializer, idColumnName)), pCreateStatement);
  }

  /**
   * Executes an insert statement.
   *
   * @param pInsertStatement the statement to execute (defined in a pipelining mechanism)
   */
  public void doInsert(Consumer<Insert> pInsertStatement)
  {
    _execute(configureStatementBeforeExecution(new Insert(_createNoResultExecutor(), databaseType, serializer, idColumnName)), pInsertStatement);
  }

  /**
   * Executes an update statement.
   *
   * @param pUpdateStatement the statement to execute (defined in a pipelining mechanism)
   */
  public void doUpdate(Consumer<Update> pUpdateStatement)
  {
    _execute(configureStatementBeforeExecution(new Update(_createNoResultExecutor(), databaseType, serializer, idColumnName)), pUpdateStatement);
  }

  /**
   * Executes a delete statement.
   *
   * @param pDeleteStatement the statement to execute (defined in a pipelining mechanism)
   */
  public boolean doDelete(Function<Delete, Boolean> pDeleteStatement)
  {
    return _query(configureStatementBeforeExecution(new Delete(_createSuccessfulExecutor(), databaseType, serializer, idColumnName)), pDeleteStatement);
  }

  /**
   * Creates a new select statement.
   *
   * @param pSelectQuery the select query to execute (defined in a pipelining mechanism)
   * @param pColumns     the columns to select
   * @param <RESULT>     the type of the result
   * @return the result of the select statement
   */
  public <RESULT> RESULT doSelect(Function<Select, RESULT> pSelectQuery, IColumnIdentification... pColumns)
  {
    Select select = pColumns == null || pColumns.length == 0 ? new Select(_createResultExecutor(), databaseType, serializer, idColumnName) :
        new Select(_createResultExecutor(), databaseType, serializer, idColumnName, pColumns);
    return _query(configureStatementBeforeExecution(select), pSelectQuery);
  }

  /**
   * Create a new single select statement.
   * This query will select one certain column only.
   *
   * @param pColumn      the single column to select
   * @param pSelectQuery the select query to execute (defined in a pipelining mechanism)
   * @param <TYPE>       the data type of the column
   * @param <RESULT>     the type of the result
   * @return the result of the select statement
   */
  public <TYPE, RESULT> RESULT doSelectOne(IColumnIdentification<TYPE> pColumn, Function<SingleSelect<TYPE>, RESULT> pSelectQuery)
  {
    SingleSelect<TYPE> select = new SingleSelect<>(_createResultExecutor(), databaseType, serializer, idColumnName, pColumn);
    return _query(configureStatementBeforeExecution(select), pSelectQuery);
  }

  /**
   * Configures a statement before it will be executed.
   * This method may be overwritten by child classes to adapt the statements in a specific way.
   *
   * @param pStatement  the statement to configure
   * @param <RESULT>    the generic type of the result of the statement
   * @param <STATEMENT> the concrete type of the statement
   */
  protected <RESULT, STATEMENT extends AbstractBaseStatement<RESULT, STATEMENT>> STATEMENT configureStatementBeforeExecution(STATEMENT pStatement)
  {
    return pStatement; //Nothing happens here, may be overwritten in child classes
  }

  /**
   * Adds a column to a database table.
   *
   * @param pTableName        the name of the table to add the column
   * @param pColumnDefinition information about the new column
   */
  protected void addColumn(String pTableName, IColumnDefinition pColumnDefinition)
  {
    _executeNoResultStatement("ALTER TABLE " + pTableName + " ADD " + pColumnDefinition.getAsDBString(databaseType));
  }

  /**
   * Checks, if a certain table exists in the database.
   *
   * @param pTableName the name of the table to check
   * @return <tt>true</tt>, if the table is existing
   */
  protected boolean hasTable(String pTableName)
  {
    Connection connection = connectionSupplier.get();
    try
    {
      ResultSet tables = connection.getMetaData().getTables(null, null, pTableName.toUpperCase(), null);
      return tables.next();
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
    finally
    {
      _tryCloseConnection(connection);
    }
  }

  /**
   * Executes a create statement, if a certain table is not existing in the database.
   *
   * @param pTableName       the name of the table to check
   * @param pCreateStatement the create statement to execute (defined in a pipelining mechanism)
   */
  protected void ifTableNotExistingCreate(String pTableName, Consumer<Create> pCreateStatement)
  {
    if (!hasTable(pTableName))
      doCreate(pCreateStatement);
  }

  /**
   * The column count of a certain table.
   *
   * @return the number of columns of a database table
   */
  protected int getColumnCount(String pTableName)
  {
    return doSelect(pSelect -> pSelect
        .from(pTableName)
        .countColumns());
  }

  /**
   * Performs a no result database statement and closes the connection afterwards, if necessary.
   *
   * @param pStatement         the initial statement to configure and to execute afterwards
   * @param pStatementConsumer a consumer of the specific statement to configure it before the execution
   * @param <STATEMENT>        the type of the specific statement created by the query builder
   */
  private <STATEMENT extends AbstractBaseStatement<Void, STATEMENT>> void _execute(STATEMENT pStatement, Consumer<STATEMENT> pStatementConsumer)
  {
    pStatementConsumer.accept(pStatement);
    _tryCloseConnection(pStatement);
  }

  /**
   * Performs a database query with a certain result and closes the connectionSupplier afterwards, if necessary.
   *
   * @param pQuery         the initial query to configure and to execute afterwards
   * @param pQueryConsumer a function that will be provided with the query to configure it before the execution
   * @param <RESULT>       the type of the result of this query
   * @param <INNERRESULT>  the type of the result of the specific {@link IStatementExecutor} for this query
   * @param <QUERY>        the type of the specific query
   * @return the result determined by the given query
   * @
   */
  private <RESULT, INNERRESULT, QUERY extends AbstractBaseStatement<INNERRESULT, QUERY>> RESULT _query(QUERY pQuery, Function<QUERY, RESULT> pQueryConsumer)
  {
    try
    {
      return pQueryConsumer.apply(pQuery);
    }
    finally
    {
      _tryCloseConnection(pQuery);
    }
  }

  /**
   * Executes a SQL statement with no result.
   * If necessary, the connection will be closed after the execution.
   *
   * @param pSQLStatement the SQL statement to execute
   */
  private void _executeNoResultStatement(String pSQLStatement)
  {
    IStatementExecutor<Void> executor = _createNoResultExecutor();
    executor.executeStatement(pSQLStatement);
    _tryCloseConnection(executor);
  }

  /**
   * Creates a statement executor for no result statements.
   *
   * @return a statement executor
   */
  private IStatementExecutor<Void> _createNoResultExecutor()
  {
    return new _StatementExecutor<>((pStatement, pSQLStatement) -> {
      pStatement.execute(pSQLStatement);
      return null;
    });
  }

  /**
   * Creates a statement executor, that will return to a {@link ResultSet}, when executed.
   *
   * @return a statement executor
   */
  private IStatementExecutor<ResultSet> _createResultExecutor()
  {
    return new _StatementExecutor<>(Statement::executeQuery);
  }

  /**
   * Creates a statement executor, that will inform about its successful execution (true/false) as result.
   *
   * @return a statement executor
   */
  private IStatementExecutor<Boolean> _createSuccessfulExecutor()
  {
    return new _StatementExecutor<>((pStatement, pSQLStatement) -> {
      try
      {
        pStatement.execute(pSQLStatement);
        return true;
      }
      catch (SQLException pE)
      {
        return false;
      }
    });
  }

  /**
   * Tries to close a database connection.
   *
   * @param pCloseable the closeable, that represents the connection
   */
  private void _tryCloseConnection(AutoCloseable pCloseable)
  {
    try
    {
      if (closeAfterStatement)
        pCloseable.close();
    }
    catch (Exception pE)
    {
      throw new OJDatabaseException(pE, "Unable to close the database connection!");
    }
  }

  /**
   * A bi-function, that may throw an exception.
   *
   * @param <ARG1>      the type of the first argument of the function
   * @param <ARG2>      the type of the second argument of the function
   * @param <RESULT>    the type of the result of the function
   * @param <EXCEPTION> the type of the exception
   */
  @FunctionalInterface
  private interface _ThrowingBiFunction<ARG1, ARG2, RESULT, EXCEPTION extends Exception>
  {
    /**
     * Returns a result based on two arguments.
     *
     * @param pArgument1 the first argument
     * @param pArgument2 the second argument
     * @return the result of this function
     * @throws EXCEPTION if, there went something wrong
     */
    RESULT apply(ARG1 pArgument1, ARG2 pArgument2) throws EXCEPTION;
  }

  /**
   * Implementation of a statement executor based on a function, that will be provided with a {@link Statement} and the SQL statement.
   * The function then should return the result of the statement
   *
   * @param <RESULT> the generic type of the result
   */
  private class _StatementExecutor<RESULT> implements IStatementExecutor<RESULT>
  {
    private final _ThrowingBiFunction<Statement, String, RESULT, SQLException> executor;
    private Connection connection = null;

    /**
     * Creates the executor.
     *
     * @param pExecutor the executing function provided with a {@link Statement} and the SQL statement
     */
    private _StatementExecutor(_ThrowingBiFunction<Statement, String, RESULT, SQLException> pExecutor)
    {
      executor = pExecutor;
    }

    @Override
    public RESULT executeStatement(String pSQLStatement)
    {
      connection = connectionSupplier.get();
      try
      {
        return executor.apply(connection.createStatement(), pSQLStatement);
      }
      catch (SQLException pE)
      {
        throw new OJDatabaseException(pSQLStatement, pE);
      }
    }

    @Override
    public void close() throws IOException
    {
      if (connection != null)
      {
        try
        {
          connection.close();
        }
        catch (SQLException pE)
        {
          throw new IOException(pE);
        }
      }
    }
  }
}
