package de.adito.ojcms.sqlbuilder.statements.types.select;

import de.adito.ojcms.sqlbuilder.AbstractSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.result.*;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;

import java.sql.ResultSet;
import java.util.Optional;

/**
 * A select statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Select extends AbstractSelect<Select>
{
  /**
   * Creates a new select statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform          the database platform used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Select(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pPlatform, pSerializer, pIdColumnName);
  }

  /**
   * Defines the columns to select. Must be at least one column.
   *
   * @param pColumns the columns to select
   * @return the select statement itself to enable a pipelining mechanism
   */
  public Select select(IColumnIdentification<?>... pColumns)
  {
    return addColumns(pColumns);
  }

  /**
   * The first result row, if present.
   *
   * @return an optional result row
   */
  public Optional<ResultRow> firstResult()
  {
    return fullResult().getFirst();
  }

  /**
   * The full result of the query (0-n rows).
   *
   * @return the result of the query
   */
  public Result fullResult()
  {
    return doQuery();
  }
}
