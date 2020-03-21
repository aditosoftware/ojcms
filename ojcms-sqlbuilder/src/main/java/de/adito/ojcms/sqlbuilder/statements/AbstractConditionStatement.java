package de.adito.ojcms.sqlbuilder.statements;

import de.adito.ojcms.sqlbuilder.*;
import de.adito.ojcms.sqlbuilder.definition.condition.*;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * An abstract base class for every statement that is based on a table and allows conditions,
 * which determine, what rows should be affected by the statement.
 *
 * @param <MODIFIERS>   the type of the modifiers used for this statement
 * @param <RESULT>      the result type of this statement
 * @param <INNERRESULT> the result type of the {@link IStatementExecutor} for this statement
 * @param <STATEMENT>   the concrete final type of the statement
 */
public abstract class AbstractConditionStatement<MODIFIERS extends WhereModifiers, RESULT, INNERRESULT,
    STATEMENT extends AbstractConditionStatement<MODIFIERS, RESULT, INNERRESULT, STATEMENT>>
    extends AbstractSQLStatement<INNERRESULT, STATEMENT>
{
  protected final MODIFIERS modifiers;

  /**
   * Creates a new statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform          the database platform used for this statement
   * @param pSerializer        the value serializer
   * @param pModifiers         the modifiers for this statement
   * @param pIdColumnName      the id column name
   */
  public AbstractConditionStatement(IStatementExecutor<INNERRESULT> pStatementExecutor, AbstractSQLBuilder pBuilder,
                                    IDatabasePlatform pPlatform, IValueSerializer pSerializer, MODIFIERS pModifiers, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pPlatform, pSerializer, pIdColumnName);
    modifiers = pModifiers;
  }

  /**
   * Executes the statement and returns the result.
   *
   * @return the result of this statement
   */
  protected abstract RESULT doQuery();

  /**
   * Sets the where condition for this statement.
   * The condition contains any amount of single where conditions that will be concatenated with "AND".
   *
   * @param pConditions the single conditions to concatenate a multiple where condition
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT where(IWhereCondition<?>... pConditions)
  {
    return where(Arrays.asList(pConditions));
  }

  /**
   * Sets the where condition for this statement.
   * The condition contains any amount of single where conditions that will be concatenated with "AND".
   *
   * @param pConditions the single conditions to concatenate a multiple where condition
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT where(List<IWhereCondition<?>> pConditions)
  {
    if (pConditions == null || pConditions.isEmpty())
      //noinspection unchecked
      return (STATEMENT) this;

    final IWhereConditions conditions = IWhereConditions.create(pConditions.get(0));
    pConditions.stream() //
        .skip(1) //
        .forEach(conditions::and);

    return where(conditions);
  }

  /**
   * Sets the where condition for this statement.
   * The condition might contain multiple, concatenated conditions.
   *
   * @param pConditions the where condition to set (can be null, then nothing happens)
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT where(@Nullable IWhereConditions pConditions)
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
  public STATEMENT whereId(long pId)
  {
    return whereId(IWhereOperator.isEqual(), pId);
  }

  /**
   * Configures the statement to only affect rows with ids that are contained in a given set.
   *
   * @param pIds the row ids the statement should affect
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT whereIdIn(Set<Long> pIds)
  {
    return whereId(IWhereConditionsForId.in(pIds, idColumnIdentification));
  }

  /**
   * Configures the statement to only affect rows with certain ids (based on a where condition).
   *
   * @param pWhereOperator the where operator for the id condition
   * @param pId            the row id for the condition
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT whereId(@NotNull IWhereOperator pWhereOperator, long pId)
  {
    return whereId(IWhereConditionsForId.create(Objects.requireNonNull(pWhereOperator), pId));
  }

  /**
   * Configures the statement to only affect rows with certain ids (based on a multiple where condition).
   *
   * @param pMultipleConditions the multiple id condition (can be null, then nothing happens)
   * @return the statement itself to enable a pipelining mechanism
   */
  public STATEMENT whereId(@Nullable IWhereConditionsForId pMultipleConditions)
  {
    modifiers.setWhereIdCondition(pMultipleConditions);
    //noinspection unchecked
    return (STATEMENT) this;
  }
}
