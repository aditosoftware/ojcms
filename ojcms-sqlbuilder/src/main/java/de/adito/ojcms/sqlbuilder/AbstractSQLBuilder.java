package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.statements.*;
import de.adito.ojcms.sqlbuilder.util.*;
import de.adito.ojcms.utils.StringUtility;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.function.*;

/**
 * Abstract base class for the SQL builder.
 * The database statements can be used and adapted in a functional way like the Java streams.
 *
 * @author Simon Danner, 19.02.2018
 */
public abstract class AbstractSQLBuilder
{
  private final EDatabaseType databaseType;
  private final DBConnectionInfo connectionInfo;
  private final boolean closeAfterStatement;
  private final Supplier<Connection> connectionSupplier;
  private final IValueSerializer serializer;
  private final String idColumnName; //global id column name for this builder

  /**
   * Creates a new builder.
   *
   * @param pDatabaseType        the database type to use for this builder
   * @param pConnectionInfo      the database connection information
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pSerializer          a value serializer
   * @param pIdColumnName        a global id column name for this builder instance
   */
  protected AbstractSQLBuilder(EDatabaseType pDatabaseType, DBConnectionInfo pConnectionInfo, boolean pCloseAfterStatement,
                               IValueSerializer pSerializer, String pIdColumnName)
  {
    databaseType = Objects.requireNonNull(pDatabaseType);
    connectionInfo = Objects.requireNonNull(pConnectionInfo);
    closeAfterStatement = pCloseAfterStatement;
    connectionSupplier = _createConnectionSupplier();
    serializer = Objects.requireNonNull(pSerializer);
    idColumnName = StringUtility.requireNotEmpty(pIdColumnName, "id column name");
    databaseType.initDriver();
  }

  /**
   * Executes a create statement.
   *
   * @param pCreateStatement the statement to execute (defined in a pipelining mechanism)
   */
  public void doCreate(Consumer<Create> pCreateStatement)
  {
    final Create statement = new Create(_createNoResultExecutor(), this, databaseType, serializer, idColumnName);
    _execute(configureStatementBeforeExecution(statement), pCreateStatement);
  }

  /**
   * Executes an insert statement.
   *
   * @param pInsertStatement the statement to execute (defined in a pipelining mechanism)
   */
  public void doInsert(Consumer<Insert> pInsertStatement)
  {
    final Insert statement = new Insert(_createNoResultExecutor(), this, databaseType, serializer, idColumnName);
    _execute(configureStatementBeforeExecution(statement), pInsertStatement);
  }

  /**
   * Executes an update statement.
   *
   * @param pUpdateStatement the statement to execute (defined in a pipelining mechanism)
   */
  public void doUpdate(Consumer<Update> pUpdateStatement)
  {
    final Update statement = new Update(_createNoResultExecutor(), this, databaseType, serializer, idColumnName);
    _execute(configureStatementBeforeExecution(statement), pUpdateStatement);
  }

  /**
   * Executes a delete statement.
   *
   * @param pDeleteStatement the statement to execute (defined in a pipelining mechanism)
   */
  public boolean doDelete(Function<Delete, Boolean> pDeleteStatement) //NOSONAR
  {
    final Delete statement = new Delete(_createSuccessfulExecutor(), this, databaseType, serializer, idColumnName);
    return _query(configureStatementBeforeExecution(statement), pDeleteStatement);
  }

  /**
   * Creates a new select statement.
   *
   * @param pSelectQuery the select query to execute (defined in a pipelining mechanism)
   * @param <RESULT>     the type of the result
   * @return the result of the select statement
   */
  public <RESULT> RESULT doSelect(Function<Select, RESULT> pSelectQuery)
  {
    final Select statement = new Select(_createResultExecutor(), this, databaseType, serializer, idColumnName);
    return _query(configureStatementBeforeExecution(statement), pSelectQuery);
  }

  /**
   * Creates a new single select statement.
   * This query will select one certain column only.
   *
   * @param pColumnToSelect the single column to select
   * @param pSelectQuery    the select query to execute (defined in a pipelining mechanism)
   * @param <VALUE>         the data type of the column
   * @param <RESULT>        the type of the result
   * @return the result of the select statement
   */
  public <VALUE, RESULT> RESULT doSelectOne(IColumnIdentification<VALUE> pColumnToSelect, Function<SingleSelect<VALUE>, RESULT> pSelectQuery)
  {
    final SingleSelect<VALUE> select = new SingleSelect<>(_createResultExecutor(), this, databaseType, serializer, idColumnName, pColumnToSelect);
    return _query(configureStatementBeforeExecution(select), pSelectQuery);
  }

  /**
   * Creates a new single select statement to select the id column of a database table.
   *
   * @param pSelectQuery the select query to execute (defined in a pipelining mechanism)
   * @param <RESULT>     the type of the result
   * @return the result of the select statement
   */
  public <RESULT> RESULT doSelectId(Function<SingleSelect<Integer>, RESULT> pSelectQuery)
  {
    final IColumnIdentification<Integer> idColumnIdentification = IColumnIdentification.of(idColumnName, Integer.class);
    final SingleSelect<Integer> select = new SingleSelect<>(_createResultExecutor(), this, databaseType, serializer, idColumnName,
                                                            idColumnIdentification);
    return _query(configureStatementBeforeExecution(select), pSelectQuery);
  }

  /**
   * Configures a statement before it will be executed.
   * This method may be overwritten by sub classes to adapt the statements in a specific way.
   *
   * @param pStatement  the statement to configure
   * @param <RESULT>    the generic type of the result of the statement
   * @param <STATEMENT> the runtime type of the statement
   */
  protected <RESULT, STATEMENT extends AbstractBaseStatement<RESULT, STATEMENT>> STATEMENT configureStatementBeforeExecution(STATEMENT pStatement)
  {
    return pStatement; //Nothing happens here, may be overwritten in sub classes
  }

  /**
   * Drops a table from the database.
   *
   * @param pTableName the name of the table to drop
   * @return <tt>true</tt>, if the table was dropped successfully
   */
  protected boolean dropTable(String pTableName)
  {
    return _createSuccessfulExecutor().executeStatement("DROP TABLE " + pTableName);
  }

  /**
   * Adds a column to a database table.
   *
   * @param pTableName        the name of the table to add the column
   * @param pColumnDefinition information about the new column
   */
  protected void addColumn(String pTableName, IColumnDefinition pColumnDefinition)
  {
    _executeNoResultStatement("ALTER TABLE " + pTableName + " ADD " + pColumnDefinition.toStatementFormat(databaseType, idColumnName));
  }

  /**
   * Removes a column from a database table.
   *
   * @param pTableName the name of the table to remove the column from
   * @param pColumn    the column to remove
   */
  protected void removeColumn(String pTableName, IColumnIdentification<?> pColumn)
  {
    _executeNoResultStatement("ALTER TABLE " + pTableName + " DROP COLUMN " + pColumn.toStatementFormat(databaseType, idColumnName));
  }

  /**
   * Checks, if a certain table exists in the database.
   *
   * @param pTableName the name of the table to check
   * @return <tt>true</tt>, if the table is existing
   */
  protected boolean hasTable(String pTableName)
  {
    return getAllTableNames().contains(pTableName.toUpperCase());
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
   * All table names of the database.
   *
   * @return a list of all table names
   */
  protected List<String> getAllTableNames()
  {
    return _retrieveFromMetaData(pMetaData -> {
      final List<String> names = new ArrayList<>();
      final ResultSet tables = pMetaData.getTables(null, null, "%", null);
      while (tables.next())
      {
        final String name = tables.getString(3);
        if (!name.startsWith(databaseType.getSystemTablesPrefix())) //Exclude system tables
          names.add(tables.getString(3));
      }
      return names;
    });
  }

  /**
   * The column count of a certain table.
   *
   * @param pTableName the name of the table to retrieve the column count from
   * @return the number of columns of a database table
   */
  protected int getColumnCount(String pTableName)
  {
    return _retrieveFromMetaData(pMetaData -> {
      final ResultSet result = pMetaData.getColumns(null, null, pTableName.toUpperCase(), null);
      int count = 0;
      while (result.next())
        count++;
      return count;
    });
  }

  /**
   * Determines, if a column name is present at a certain database table.
   *
   * @param pTableName  the name of the database table
   * @param pColumnName the name of the column to check
   * @return <tt>true</tt> if the column is present
   */
  protected boolean hasColumn(String pTableName, String pColumnName)
  {
    return _retrieveFromMetaData(pMetaData -> pMetaData.getColumns(null, null, pTableName.toUpperCase(), pColumnName.toUpperCase()).next());
  }

  /**
   * The database type of the builder.
   *
   * @return a database type
   */
  EDatabaseType getDatabaseType()
  {
    return databaseType;
  }

  /**
   * The database connection information of the builder.
   *
   * @return the database connection information
   */
  public DBConnectionInfo getConnectionInfo()
  {
    return connectionInfo;
  }

  /**
   * Determines, if this builder closes database connections after executing a statement.
   *
   * @return <tt>true</tt>, if connections will be closed
   */
  boolean closeAfterStatement()
  {
    return closeAfterStatement;
  }

  /**
   * The value serializer of this builder.
   *
   * @return a value serializer
   */
  IValueSerializer getSerializer()
  {
    return serializer;
  }

  /**
   * The global id column name used for this builder.
   *
   * @return the global id column name
   */
  String getIdColumnName()
  {
    return idColumnName;
  }

  /**
   * Creates the connection supplier for this builder.
   * If connections should be closed after every statement, a supplier returning new connections on every call will be returned.
   * Otherwise one connection will be created, that is always returned by the resulting supplier.
   *
   * @return a connection supplier
   */
  private Supplier<Connection> _createConnectionSupplier()
  {
    if (closeAfterStatement)
      return connectionInfo::createConnection;
    final Connection permanentConnection = connectionInfo.createConnection(); //NOSONAR
    return () -> permanentConnection;
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
   * Performs a database query with a certain result and closes the connection supplier afterwards, if necessary.
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
   * @param pArgs         arguments for prepared statements
   */
  private void _executeNoResultStatement(String pSQLStatement, String... pArgs)
  {
    _executeNoResultStatement(pSQLStatement, Arrays.asList(pArgs));
  }

  /**
   * Executes a SQL statement with no result.
   * If necessary, the connection will be closed after the execution.
   *
   * @param pSQLStatement the SQL statement to execute
   * @param pArgs         arguments for prepared statements
   */
  private void _executeNoResultStatement(String pSQLStatement, List<String> pArgs)
  {
    final IStatementExecutor<Void> executor = _createNoResultExecutor();
    executor.executeStatement(pSQLStatement, pArgs);
    _tryCloseConnection(executor);
  }

  /**
   * Creates a statement executor for no result statements.
   *
   * @return a statement executor
   */
  private IStatementExecutor<Void> _createNoResultExecutor()
  {
    return new _StatementExecutor<>(pStatement -> {
      pStatement.execute();
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
    return new _StatementExecutor<>(PreparedStatement::executeQuery);
  }

  /**
   * Creates a statement executor, that will inform about its successful execution (true/false) as result.
   *
   * @return a statement executor
   */
  private IStatementExecutor<Boolean> _createSuccessfulExecutor()
  {
    return new _StatementExecutor<>(pStatement -> {
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
   * Retrieves any information of the metadata of a database connection.
   * The connection will be closed afterwards. Do not keep a reference to the metadata instance.
   *
   * @param pResultResolver a function to retrieve the information from the database metadata
   * @param <RESULT>        the type of the information/result
   * @return the information from the database metadata
   */
  private <RESULT> RESULT _retrieveFromMetaData(_ThrowingFunction<DatabaseMetaData, RESULT, SQLException> pResultResolver)
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
      _tryCloseConnection(connection);
    }
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
   * A function that may throw an exception.
   *
   * @param <ARG>       the type of the argument of the function
   * @param <RESULT>    the type of the result of the function
   * @param <EXCEPTION> the type of the exception
   */
  @FunctionalInterface
  private interface _ThrowingFunction<ARG, RESULT, EXCEPTION extends Exception>
  {
    /**
     * Returns a result based on one argument.
     *
     * @param pArgument the argument
     * @return the result of this function
     * @throws EXCEPTION if something went wrong there
     */
    RESULT apply(ARG pArgument) throws EXCEPTION;
  }

  /**
   * Implementation of a statement executor based on a function, that will be provided with a {@link PreparedStatement}.
   * The function then should return the result of the statement.
   *
   * @param <RESULT> the generic type of the result
   */
  private class _StatementExecutor<RESULT> implements IStatementExecutor<RESULT>
  {
    private final _ThrowingFunction<PreparedStatement, RESULT, SQLException> executor;
    private Connection connection;
    private PreparedStatement statement;

    /**
     * Creates the executor.
     *
     * @param pExecutor the executing function provided with a {@link PreparedStatement}
     */
    private _StatementExecutor(_ThrowingFunction<PreparedStatement, RESULT, SQLException> pExecutor)
    {
      executor = pExecutor;
    }

    @Override
    public RESULT executeStatement(String pSQLStatement, List<String> pArgs)
    {
      connection = connectionSupplier.get();
      try
      {
        statement = connection.prepareStatement(pSQLStatement); //NOSONAR
        int argIndex = 1;
        for (String arg : pArgs)
          statement.setString(argIndex++, arg);
        return executor.apply(statement);
      }
      catch (SQLException pE)
      {
        throw new OJDatabaseException(pSQLStatement, pE);
      }
    }

    @Override
    public void close() throws IOException
    {
      try
      {
        if (statement != null)
          statement.close();
        if (connection != null)
          connection.close();
      }
      catch (SQLException pE)
      {
        throw new IOException(pE);
      }
    }
  }
}
