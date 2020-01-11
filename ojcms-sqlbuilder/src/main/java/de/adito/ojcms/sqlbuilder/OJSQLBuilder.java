package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.statements.types.Create;

import java.util.List;
import java.util.function.Consumer;

/**
 * A SQL statement builder.
 * The database statements can be used and adapted in a functional way like the Java streams.
 * This builder can be used for multiple database tables.
 * This builder can be configured via {@link OJSQLBuilderFactory}.
 *
 * @author Simon Danner, 01.01.2020
 */
public interface OJSQLBuilder extends IBaseBuilder
{
  /**
   * Drops a table from the database.
   *
   * @param pTableName the name of the table to drop
   * @return <tt>true</tt>, if the table was dropped successfully
   */
  boolean dropTable(String pTableName);

  /**
   * Adds a column to a database table.
   *
   * @param pTableName        the name of the table to add the column
   * @param pColumnDefinition information about the new column
   */
  void addColumn(String pTableName, IColumnDefinition pColumnDefinition);

  /**
   * Removes a column from a database table.
   *
   * @param pTableName the name of the table to remove the column from
   * @param pColumn    the column to remove
   */
  void removeColumn(String pTableName, IColumnIdentification<?> pColumn);

  /**
   * Checks, if a certain table exists in the database.
   *
   * @param pTableName the name of the table to check
   * @return <tt>true</tt>, if the table is existing
   */
  boolean hasTable(String pTableName);

  /**
   * Executes a create statement, if a certain table is not existing in the database.
   *
   * @param pTableName       the name of the table to check
   * @param pCreateStatement the create statement to execute (defined in a pipelining mechanism)
   */
  void ifTableNotExistingCreate(String pTableName, Consumer<Create> pCreateStatement);

  /**
   * All table names of the database.
   *
   * @return a list of all table names
   */
  List<String> getAllTableNames();

  /**
   * The column count of a certain table.
   *
   * @param pTableName the name of the table to retrieve the column count from
   * @return the number of columns of a database table
   */
  int getColumnCount(String pTableName);

  /**
   * Determines, if a column name is present at a certain database table.
   *
   * @param pTableName  the name of the database table
   * @param pColumnName the name of the column to check
   * @return <tt>true</tt> if the column is present
   */
  boolean hasColumn(String pTableName, String pColumnName);
}
