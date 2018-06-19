package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.*;
import de.adito.beans.persistence.datastores.sql.builder.modifiers.WhereModifiers;

import java.util.stream.Stream;

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
   * @param pSerializer        the value serializer
   * @param pModifiers         the modifiers for this statement
   */
  public AbstractSQLStatement(IStatementExecutor<INNERRESULT> pStatementExecutor, EDatabaseType pDatabaseType, IValueSerializer pSerializer,
                              MODIFIERS pModifiers)
  {
    super(pStatementExecutor, pDatabaseType, pSerializer);
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
   * Sets the where condition for this statement.
   * The condition contains any amount of single where conditions, which will be concatenated with "AND".
   *
   * @param pConditions the single conditions to concatenate to a multiple where condition
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT where(IWhereCondition<?>... pConditions)
  {
    if (pConditions.length == 0)
      //noinspection unchecked
      return (STATEMENT) this;

    IWhereConditions conditions = IWhereConditions.create(pConditions[0]);
    Stream.of(pConditions)
        .skip(1)
        .forEach(conditions::and);
    return where(conditions);
  }

  /**
   * Sets the where condition for this statement.
   * The condition might contain multiple, concatenated conditions.
   *
   * @param pConditions the where condition to set
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT where(IWhereConditions pConditions)
  {
    modifiers.setWhereCondition(pConditions);
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
    modifiers.setWhereId(pId);
    //noinspection unchecked
    return (STATEMENT) this;
  }
}
