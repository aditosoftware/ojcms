package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.result.SingleColumnResult;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.sql.ResultSet;

/**
 * A single column select statement.
 *
 * @param <TYPE> the type of the result
 * @author Simon Danner, 26.04.2018
 */
public class SingleSelect<TYPE> extends AbstractSelect<SingleSelect<TYPE>>
{
  private final IColumnIdentification<TYPE> column;

  /**
   * Creates a new select statement based on single column.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pIdColumnName      the name of the id column
   * @param pColumn            the single column to select
   */
  public SingleSelect(IStatementExecutor<ResultSet> pStatementExecutor, EDatabaseType pDatabaseType, String pIdColumnName,
                      IColumnIdentification<TYPE> pColumn)
  {
    super(pStatementExecutor, pDatabaseType, pIdColumnName, pColumn);
    column = pColumn;
  }

  /**
   * The result of the first row.
   * The result is optional because there may not be a first row.
   *
   * @return a optional result of the query (first row)
   */
  public OptionalNullable<TYPE> firstResult()
  {
    return fullResult().getFirst();
  }

  /**
   * The full result of the query.
   * In this case this is a set of values of the field's data type for each result row.
   *
   * @return the result of the query
   */
  public SingleColumnResult<TYPE> fullResult()
  {
    return new SingleColumnResult<>(column, doQuery());
  }
}
