package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.*;

import java.util.stream.Stream;

/**
 * An abstract base class for every statement, which is based on a table and allows conditions,
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
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pModifiers         the modifiers for this statement
   */
  public AbstractSQLStatement(IStatementExecutor<INNERRESULT> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                              IValueSerializer pSerializer, MODIFIERS pModifiers)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer);
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
    return whereId(IWhereOperator.isEqual(), pId);
  }

  /**
   * Configures the statement to only affect rows with certain ids (based on a where condition).
   *
   * @param pWhereOperator the where operator for the id condition
   * @param pId            the row id for the condition
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT whereId(IWhereOperator pWhereOperator, int pId)
  {
    return whereId(IWhereConditionsForId.create(pWhereOperator, pId));
  }

  /**
   * Configures the statement to only affect rows with certain ids (based on a multiple where condition).
   *
   * @param pMultipleConditions the multiple id condition
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT whereId(IWhereConditionsForId pMultipleConditions)
  {
    modifiers.setWhereIdCondition(pMultipleConditions);
    //noinspection unchecked
    return (STATEMENT) this;
  }
}
