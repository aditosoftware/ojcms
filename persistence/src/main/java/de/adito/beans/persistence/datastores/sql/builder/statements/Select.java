package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.result.*;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

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
   * @param pDatabaseType      the database type used for this statement
   * @param pIdColumnName      the id column name
   */
  public Select(IStatementExecutor<ResultSet> pStatementExecutor, EDatabaseType pDatabaseType, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType, pIdColumnName);
  }

  /**
   * Creates a new select statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pIdColumnName      the id column name
   * @param pColumns           the  columns to select
   */
  public Select(IStatementExecutor<ResultSet> pStatementExecutor, EDatabaseType pDatabaseType, String pIdColumnName,
                IColumnIdentification<?>... pColumns)
  {
    super(pStatementExecutor, pDatabaseType, pIdColumnName, pColumns);
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
