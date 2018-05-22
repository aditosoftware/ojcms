package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.modifiers.WhereModifiers;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

/**
 * An abstract base for every statement, which is based on a table and allows conditions,
 * which determine, what rows should be affected by the statement. (from + where)
 *
 * @param <MODIFIERS>   the type of the modifiers used for this statement
 * @param <RESULT>      the result type of this statement
 * @param <INNERRESULT> the result type of the {@link IStatementExecutor} for this statement
 * @param <STATEMENT>   the concrete final type of the statement
 */
public abstract class AbstractSQLStatement<MODIFIERS extends WhereModifiers, RESULT, INNERRESULT,
    STATEMENT extends AbstractSQLStatement<MODIFIERS, RESULT, INNERRESULT, STATEMENT>> extends AbstractBaseStatement<INNERRESULT, STATEMENT>
{
  protected final MODIFIERS modifiers;

  /**
   * Creates a new statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pModifiers         the modifiers for this statement
   */
  public AbstractSQLStatement(IStatementExecutor<INNERRESULT> pStatementExecutor, EDatabaseType pDatabaseType, MODIFIERS pModifiers)
  {
    super(pStatementExecutor, pDatabaseType);
    modifiers = pModifiers;
  }

  /**
   * Executes the statement and returns the result.
   *
   * @return the result of this statement
   */
  protected abstract RESULT doQuery();

  /**
   * Determines, on which database table this statement should be executed.
   *
   * @param pTableName the name of the database table
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT from(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Determines a where condition to only affect certain rows through this statement.
   * The conditions are defined in a formal of 'COLUMN_NAME = VALUE'.
   * So one condition is represented by a {@link IColumnValueTuple}.
   *
   * @param pConditions a collection of conditions (column name + value)
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT where(IColumnValueTuple<?>... pConditions)
  {
    modifiers.setWhereConditions(pConditions);
    //noinspection unchecked
    return (STATEMENT) this;
  }

  /**
   * Configures the statement to only affect one row with a certain row id.
   *
   * @param pId the row id
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT whereId(int pId)
  {
    modifiers.setId(pId);
    //noinspection unchecked
    return (STATEMENT) this;
  }
}
