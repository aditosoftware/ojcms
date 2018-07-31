package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.result.*;

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
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Select(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, pIdColumnName);
  }

  /**
   * Creates a new select statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   * @param pColumns           the columns to select
   */
  public Select(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, String pIdColumnName, IColumnIdentification<?>... pColumns)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, pIdColumnName, pColumns);
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
