package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.executors.StatementExecution;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.platform.connection.IDatabaseConnectionSupplier;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.types.*;
import de.adito.ojcms.sqlbuilder.statements.types.select.*;
import de.adito.ojcms.utils.StringUtility;

import java.sql.*;
import java.util.*;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for the SQL builder.
 * The database statements can be used and adapted in a functional way like the Java streams.
 *
 * @author Simon Danner, 19.02.2018
 */
public abstract class AbstractSQLBuilder implements IBaseBuilder
{
  private final IDatabasePlatform platform;
  private final IDatabaseConnectionSupplier platformConnectionSupplier;
  private final StatementExecution execution;
  private final IValueSerializer serializer;
  private final String idColumnName; //global id column name for this builder

  /**
   * Creates a new builder.
   *
   * @param pPlatform                   the database platform to use for this builder
   * @param pPlatformConnectionSupplier the platform specific connection supplier
   * @param pCloseAfterExecution        <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pSerializer                 a value serializer
   * @param pIdColumnName               a global id column name for this builder instance
   */
  protected AbstractSQLBuilder(IDatabasePlatform pPlatform, IDatabaseConnectionSupplier pPlatformConnectionSupplier,
                               boolean pCloseAfterExecution, IValueSerializer pSerializer, String pIdColumnName)
  {
    platform = requireNonNull(pPlatform);
    platformConnectionSupplier = requireNonNull(pPlatformConnectionSupplier);
    execution = new StatementExecution(_createConnectionSupplier(pCloseAfterExecution), pCloseAfterExecution);
    serializer = requireNonNull(pSerializer);
    idColumnName = StringUtility.requireNotEmpty(pIdColumnName, "id column name");

    platform.initDriver();
  }

  @Override
  public void doCreate(Consumer<Create> pCreateStatement)
  {
    final Create statement = new Create(execution.createVoidExecutor(), this, platform, serializer, idColumnName);
    execution.execute(configureStatementBeforeExecution(statement), pCreateStatement);
  }

  @Override
  public void doInsert(Consumer<Insert> pInsertStatement)
  {
    final Insert statement = new Insert(execution.createVoidExecutor(), this, platform, serializer, idColumnName);
    execution.execute(configureStatementBeforeExecution(statement), pInsertStatement);
  }

  @Override
  public void doUpdate(Consumer<Update> pUpdateStatement)
  {
    final Update statement = new Update(execution.createVoidExecutor(), this, platform, serializer, idColumnName);
    execution.execute(configureStatementBeforeExecution(statement), pUpdateStatement);
  }

  @Override
  public boolean doDelete(Function<Delete, Boolean> pDeleteStatement) //NOSONAR
  {
    final Delete statement = new Delete(execution.createSuccessExecutor(), this, platform, serializer, idColumnName);
    return execution.query(configureStatementBeforeExecution(statement), pDeleteStatement);
  }

  @Override
  public <RESULT> RESULT doSelect(Function<Select, RESULT> pSelectQuery)
  {
    final Select statement = new Select(execution.createExecutor(), this, platform, serializer, idColumnName);
    return execution.query(configureStatementBeforeExecution(statement), pSelectQuery);
  }

  @Override
  public <VALUE, RESULT> RESULT doSelectOne(IColumnIdentification<VALUE> pColumnToSelect, Function<SingleSelect<VALUE>, RESULT> pSelectQuery)
  {
    final SingleSelect<VALUE> select = new SingleSelect<>(execution.createExecutor(), this, platform, serializer,
                                                          idColumnName, pColumnToSelect);

    return execution.query(configureStatementBeforeExecution(select), pSelectQuery);
  }

  @Override
  public <RESULT> RESULT doSelectId(Function<SingleSelect<Integer>, RESULT> pSelectQuery)
  {
    final IColumnIdentification<Integer> idColumnIdentification = IColumnIdentification.of(idColumnName, Integer.class);
    final SingleSelect<Integer> select = new SingleSelect<>(execution.createExecutor(), this, platform, serializer, idColumnName,
                                                            idColumnIdentification);

    return execution.query(configureStatementBeforeExecution(select), pSelectQuery);
  }

  @Override
  public IDatabaseConnectionSupplier getPlatformConnectionSupplier()
  {
    return platformConnectionSupplier;
  }

  /**
   * Configures a statement before it will be executed.
   * This method may be overwritten by sub classes to adapt the statements in a specific way.
   *
   * @param pStatement  the statement to configure
   * @param <RESULT>    the generic type of the result of the statement
   * @param <STATEMENT> the runtime type of the statement
   */
  protected <RESULT, STATEMENT extends AbstractSQLStatement<RESULT, STATEMENT>> STATEMENT configureStatementBeforeExecution(STATEMENT pStatement)
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
    return execution.createSuccessExecutor().executeStatement("DROP TABLE " + pTableName);
  }

  /**
   * Adds a column to a database table.
   *
   * @param pTableName        the name of the table to add the column
   * @param pColumnDefinition information about the new column
   */
  protected void addColumn(String pTableName, IColumnDefinition pColumnDefinition)
  {
    execution.executeVoidStatement("ALTER TABLE " + pTableName + " ADD " +
                                       pColumnDefinition.toStatementFormat(platform, idColumnName));
  }

  /**
   * Removes a column from a database table.
   *
   * @param pTableName the name of the table to remove the column from
   * @param pColumn    the column to remove
   */
  protected void removeColumn(String pTableName, IColumnIdentification<?> pColumn)
  {
    execution.executeVoidStatement("ALTER TABLE " + pTableName + " DROP COLUMN " +
                                       pColumn.toStatementFormat(platform, idColumnName));
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
      doCreate(pCreate ->
               {
                 pCreate.tableName(pTableName); //Add table name for convenience
                 pCreateStatement.accept(pCreate);
               });
  }

  /**
   * All table names of the database.
   *
   * @return a list of all table names
   */
  protected List<String> getAllTableNames()
  {
    return execution.retrieveFromMetaData(pMetaData -> {
      final List<String> names = new ArrayList<>();
      final ResultSet tables = pMetaData.getTables(null, null, "%", null);
      while (tables.next())
      {
        final String name = tables.getString(3);
        if (!name.startsWith(platform.getSystemTablePrefix())) //Exclude system tables
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
    return execution.retrieveFromMetaData(pMetaData -> {
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
    return execution.retrieveFromMetaData(pMetaData -> pMetaData.getColumns(null, null, pTableName.toUpperCase(),
                                                                            pColumnName.toUpperCase()).next());
  }

  /**
   * The database platform of the builder.
   *
   * @return a database platform
   */
  IDatabasePlatform getDatabasePlatform()
  {
    return platform;
  }

  /**
   * Determines, if this builder closes database connections after executing a statement.
   *
   * @return <tt>true</tt>, if connections will be closed
   */
  boolean closeAfterStatement()
  {
    return execution.isConnectionClosedAfterExecution();
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
   * @param pCloseAfterExecution <tt>true</tt> if SQL connections are closed after every execution
   * @return a connection supplier
   */
  private Supplier<Connection> _createConnectionSupplier(boolean pCloseAfterExecution)
  {
    if (pCloseAfterExecution)
      return platformConnectionSupplier::createNewConnection;

    final Connection permanentConnection = platformConnectionSupplier.createNewConnection(); //NOSONAR
    return () -> permanentConnection;
  }
}
