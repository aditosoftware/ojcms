package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.result.SingleColumnResult;
import de.adito.beans.persistence.datastores.sql.builder.util.OptionalNullable;

import java.sql.ResultSet;

/**
 * A single column select statement.
 *
 * @param <VALUE> the type of the result
 * @author Simon Danner, 26.04.2018
 */
public class SingleSelect<VALUE> extends AbstractSelect<SingleSelect<VALUE>>
{
  private final IColumnIdentification<VALUE> column;

  /**
   * Creates a new select statement based on single column.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column
   * @param pColumn            the single column to select
   */
  public SingleSelect(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                      IValueSerializer pSerializer, String pIdColumnName, IColumnIdentification<VALUE> pColumn)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, pIdColumnName, pColumn);
    column = pColumn;
  }

  /**
   * The result of the first row.
   * The result is optional because there may not be a first row.
   *
   * @return a optional result of the query (first row)
   */
  public OptionalNullable<VALUE> firstResult()
  {
    return fullResult().getFirst();
  }

  /**
   * The full result of the query.
   * In this case this is a set of values of the field's data type for each result row.
   *
   * @return the result of the query
   */
  public SingleColumnResult<VALUE> fullResult()
  {
    return new SingleColumnResult<>(column, doQuery());
  }
}
