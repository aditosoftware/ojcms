package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.platform.connection.IDatabaseConnectionSupplier;
import de.adito.ojcms.sqlbuilder.statements.types.*;
import de.adito.ojcms.sqlbuilder.statements.types.select.*;

import java.util.function.*;

/**
 * Base interface for a SQL builder.
 *
 * @author Simon Danner, 01.01.2020
 */
interface IBaseBuilder
{
  /**
   * Executes a create statement.
   *
   * @param pCreateStatement the statement to execute (defined in a pipelining mechanism)
   */
  void doCreate(Consumer<Create> pCreateStatement);

  /**
   * Executes an insert statement.
   *
   * @param pInsertStatement the statement to execute (defined in a pipelining mechanism)
   */
  void doInsert(Consumer<Insert> pInsertStatement);

  /**
   * Executes an update statement.
   *
   * @param pUpdateStatement the statement to execute (defined in a pipelining mechanism)
   */
  void doUpdate(Consumer<Update> pUpdateStatement);

  /**
   * Executes a delete statement.
   *
   * @param pDeleteStatement the statement to execute (defined in a pipelining mechanism)
   */
  boolean doDelete(Function<Delete, Boolean> pDeleteStatement); //NOSONAR

  /**
   * Creates a new select statement.
   *
   * @param pSelectQuery the select query to execute (defined in a pipelining mechanism)
   * @param <RESULT>     the type of the result
   * @return the result of the select statement
   */
  <RESULT> RESULT doSelect(Function<Select, RESULT> pSelectQuery);

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
  <VALUE, RESULT> RESULT doSelectOne(IColumnIdentification<VALUE> pColumnToSelect, Function<SingleSelect<VALUE>, RESULT> pSelectQuery);

  /**
   * Creates a new single select statement to select the id column of a database table.
   *
   * @param pSelectQuery the select query to execute (defined in a pipelining mechanism)
   * @param <RESULT>     the type of the result
   * @return the result of the select statement
   */
  <RESULT> RESULT doSelectId(Function<SingleSelect<Integer>, RESULT> pSelectQuery);

  /**
   * The platform specific connection supplier of the builder.
   *
   * @return the platform specific connection supplier
   */
  IDatabaseConnectionSupplier getPlatformConnectionSupplier();
}
