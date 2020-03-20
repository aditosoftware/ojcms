package de.adito.ojcms.sqlbuilder.statements.types;

import de.adito.ojcms.sqlbuilder.AbstractSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.condition.WhereModifiers;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.AbstractConditionStatement;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.util.*;

import static de.adito.ojcms.sqlbuilder.format.EFormatConstant.SET;
import static de.adito.ojcms.sqlbuilder.format.EFormatter.UPDATE;
import static de.adito.ojcms.sqlbuilder.format.ESeparator.COMMA_WITH_WHITESPACE;

/**
 * An update statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Update extends AbstractConditionStatement<WhereModifiers, Void, Void, Update>
{
  private final List<IColumnValueTuple<?>> changes = new ArrayList<>();
  private final List<INumericValueAdaption<?>> updateOldValues = new ArrayList<>();

  /**
   * Creates a new update statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform          the database platform used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Update(IStatementExecutor<Void> pStatementExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pPlatform, pSerializer, new WhereModifiers(), pIdColumnName);
  }

  /**
   * Determines, on which database table this statement should be executed.
   *
   * @param pTableName the name of the database table
   * @return the statement itself to enable a pipelining mechanism
   */
  public Update table(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Determines tuples (column + value) to update in the given table.
   *
   * @param pChanges the changes defined as column value tuples
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update set(IColumnValueTuple<?>... pChanges)
  {
    return set(Arrays.asList(pChanges));
  }

  /**
   * Determines tuples (column + value) to update in the given table.
   *
   * @param pChanges the changes defined as column value tuples
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update set(Collection<IColumnValueTuple<?>> pChanges)
  {
    changes.addAll(pChanges);
    return this;
  }

  /**
   * Sets the id column value trough this statement.
   *
   * @param pNewId the new id
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update setId(long pNewId)
  {
    return set(IColumnValueTuple.of(idColumnIdentification, pNewId));
  }

  /**
   * Changes numeric values based on the previous value.
   *
   * @param pAdaptations the columns to adapt
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update adaptNumericValue(INumericValueAdaption<?>... pAdaptations)
  {
    updateOldValues.addAll(Arrays.asList(pAdaptations));
    return this;
  }

  /**
   * Changes the id column based on the previous id.
   *
   * @param pOperation a numeric operation on the old id
   * @param pNumber    a number to apply on the numeric operation and the old id
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update adaptId(ENumericOperation pOperation, long pNumber)
  {
    return adaptNumericValue(INumericValueAdaption.of(idColumnIdentification, pOperation, pNumber));
  }

  /**
   * Performs the update in the database.
   */
  public void update()
  {
    doQuery();
  }

  @Override
  protected Void doQuery()
  {
    if (changes.isEmpty() && updateOldValues.isEmpty())
      throw new OJDatabaseException("At least one value must be updated in an update statement!");

    return executeStatement(UPDATE.create(databasePlatform, idColumnIdentification.getColumnName()) //
        .appendTableName(getTableName()) //
        .appendConstant(SET) //
        .conditional(!changes.isEmpty(), pFormatter -> pFormatter.appendMultiplePrepared(changes.stream(), COMMA_WITH_WHITESPACE)) //
        .conditional(!updateOldValues.isEmpty(), pFormatter ->
        {
          pFormatter.conditional(!changes.isEmpty(), pInnerFormatter -> pInnerFormatter.appendSeparator(COMMA_WITH_WHITESPACE));
          pFormatter.appendMultiple(updateOldValues.stream(), COMMA_WITH_WHITESPACE);
        }) //
        .appendWhereCondition(modifiers));
  }
}
