package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.statements.Create;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.sql.Connection;
import java.util.function.*;

/**
 * A sql statement builder.
 * The database statements can be used and adapted in an functional way like the Java streams.
 * This builder can be used for multiple database tables.
 * This builder can be configured via {@link OJSQLBuilderFactory}.
 *
 * @author Simon Danner, 15.05.2018
 */
public class OJSQLBuilder extends AbstractSQLBuilder
{
  /**
   * Creates a new builder.
   *
   * @param pDatabaseType        the database type to use for this builder
   * @param pConnectionSupplier  the database connection supplier
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pIdColumnName        a global id column name for this builder instance
   */
  OJSQLBuilder(EDatabaseType pDatabaseType, Supplier<Connection> pConnectionSupplier, boolean pCloseAfterStatement, String pIdColumnName)
  {
    super(pDatabaseType, pConnectionSupplier, pCloseAfterStatement, pIdColumnName);
  }

  /**
   * Adds a column to a database table.
   *
   * @param pTableName        the name of the table to add the column
   * @param pColumnDefinition information about the new column
   */
  public void addColumn(String pTableName, IColumnDefinition pColumnDefinition)
  {
    super.addColumn(pTableName, pColumnDefinition);
  }

  /**
   * Checks, if a certain table exists in the database.
   *
   * @param pTableName the name of the table to check
   * @return <tt>true</tt>, if the table is existing
   */
  public boolean hasTable(String pTableName)
  {
    return super.hasTable(pTableName);
  }

  /**
   * Executes a create statement, if a certain table is not existing in the database.
   *
   * @param pTableName       the name of the table to check
   * @param pCreateStatement the create statement to execute (defined in a pipelining mechanism)
   */
  public void ifTableNotExistingCreate(String pTableName, Consumer<Create> pCreateStatement)
  {
    super.ifTableNotExistingCreate(pTableName, pCreateStatement);
  }

  /**
   * The column count of a certain table.
   *
   * @return the number of columns of a database table
   */
  public int getColumnCount(String pTableName)
  {
    return super.getColumnCount(pTableName);
  }
}
