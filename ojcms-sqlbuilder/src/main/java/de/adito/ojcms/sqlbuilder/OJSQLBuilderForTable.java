package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.statements.types.Create;

import java.util.function.Consumer;

/**
 * A SQL statement builder for a single database table.
 * The database statements can be used and adapted in an functional way like the Java streams.
 * This builder can be configured via {@link OJSQLBuilderFactory}.
 *
 * @author Simon Danner, 01.01.2020
 */
public interface OJSQLBuilderForTable extends IBaseBuilder
{
  /**
   * Drops the table of the builder.
   */
  void dropTable();

  /**
   * Adds a column to the database table of this builder.
   *
   * @param pColumnDefinition information about the new column
   */
  void addColumn(IColumnDefinition pColumnDefinition);

  /**
   * Removes a column from the database table of this builder.
   *
   * @param pColumn the column to remove
   */
  void removeColumn(IColumnIdentification<?> pColumn);

  /**
   * Checks, if the table of this builder exists in the database.
   *
   * @return <tt>true</tt>, if the table is existing
   */
  boolean hasTable();

  /**
   * Executes a create statement, if the table of this builder is not existing in the database.
   *
   * @param pCreateStatement the create statement to execute (defined in a pipelining mechanism)
   */
  void ifTableNotExistingCreate(Consumer<Create> pCreateStatement);

  /**
   * The column count of the table of this builder.
   *
   * @return the number of columns of the database table
   */
  int getColumnCount();

  /**
   * Determines, if a column is present.
   *
   * @param pColumnName the name of the column to check
   * @return <tt>true</tt> if the column is present
   */
  boolean hasColumn(String pColumnName);
}
