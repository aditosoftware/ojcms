package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.statements.Create;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.sql.Connection;
import java.util.function.*;

/**
 * A sql statement builder for a single database table.
 * The database statements can be used and adapted in an functional way like the Java streams.
 * This builder can be configured via {@link OJSQLBuilderFactory}.
 *
 * @author Simon Danner, 15.05.2018
 */
public class OJSQLBuilderForTable extends AbstractSQLBuilder
{
  private final String tableName;

  /**
   * Creates a new builder.
   *
   * @param pDatabaseType        the database type to use for this builder
   * @param pConnectionSupplier  the database connection supplier
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pTableName           the name of the table to use for this builder
   * @param pIdColumnName        a global id column name for this builder instance
   */
  OJSQLBuilderForTable(EDatabaseType pDatabaseType, Supplier<Connection> pConnectionSupplier, boolean pCloseAfterStatement,
                       String pTableName, String pIdColumnName)
  {
    super(pDatabaseType, pConnectionSupplier, pCloseAfterStatement, pIdColumnName);
    if (pTableName == null || pTableName.isEmpty())
      throw new IllegalArgumentException("The table name must not be null or empty!");
    tableName = pTableName;
  }

  /**
   * Adds a column to the database table of this builder.
   *
   * @param pColumnDefinition information about the new column
   */
  public void addColumn(IColumnDefinition pColumnDefinition)
  {
    super.addColumn(tableName, pColumnDefinition);
  }

  /**
   * Checks, if a the table of this builder exists in the database.
   *
   * @return <tt>true</tt>, if the table is existing
   */
  public boolean hasTable()
  {
    return super.hasTable(tableName);
  }

  /**
   * Executes a create statement, if a the table of this builder is not existing in the database.
   *
   * @param pCreateStatement the create statement to execute (defined in a pipelining mechanism)
   */
  public void ifTableNotExistingCreate(Consumer<Create> pCreateStatement)
  {
    super.ifTableNotExistingCreate(tableName, pCreateStatement);
  }

  /**
   * The column count of the table of this builder.
   *
   * @return the number of columns of the database table
   */
  public int getColumnCount()
  {
    return super.getColumnCount(tableName);
  }

  @Override
  protected <RESULT, STATEMENT extends AbstractBaseStatement<RESULT, STATEMENT>> STATEMENT configureStatementBeforeExecution(STATEMENT pStatement)
  {
    pStatement.setTableName(tableName);
    return pStatement;
  }
}
