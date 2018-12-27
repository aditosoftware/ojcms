package de.adito.ojcms.sqlbuilder.statements;

import de.adito.ojcms.sqlbuilder.*;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.definition.condition.WhereModifiers;
import de.adito.ojcms.sqlbuilder.format.*;

import java.util.*;

import static de.adito.ojcms.sqlbuilder.format.EFormatConstant.*;
import static de.adito.ojcms.sqlbuilder.format.EFormatter.*;
import static de.adito.ojcms.sqlbuilder.format.ESeparator.*;

/**
 * An update statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Update extends AbstractSQLStatement<WhereModifiers, Void, Void, Update>
{
  private final List<IColumnValueTuple<?>> changes = new ArrayList<>();
  private final List<INumericValueAdaption<?>> updateOldValues = new ArrayList<>();

  /**
   * Creates a new update statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Update(IStatementExecutor<Void> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, new WhereModifiers(), pIdColumnName);
  }

  /**
   * Determines tuples (column + value) to update in the given table.
   *
   * @param pChanges the changes defined as column value tuples
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update set(IColumnValueTuple<?>... pChanges)
  {
    changes.addAll(Arrays.asList(pChanges));
    return this;
  }

  /**
   * Sets the id column value trough this statement.
   *
   * @param pNewId the new id
   * @return the update statement itself to enable a pipelining mechanism
   */
  public Update setId(int pNewId)
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
  public Update adaptId(ENumericOperation pOperation, int pNumber)
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
    if (!changes.isEmpty() || !updateOldValues.isEmpty())
    {
      final StatementFormatter statement = UPDATE.create(databaseType, idColumnIdentification.getColumnName())
          .appendTableName(getTableName())
          .appendConstant(SET)
          .conditional(!changes.isEmpty(), pFormatter -> pFormatter.appendMultiplePrepared(changes.stream(), COMMA_WITH_WHITESPACE))
          .conditional(!updateOldValues.isEmpty(), pFormatter -> {
            pFormatter.conditional(!changes.isEmpty(), pInnerFormatter -> pInnerFormatter.appendSeparator(COMMA_WITH_WHITESPACE));
            pFormatter.appendMultiple(updateOldValues.stream(), COMMA_WITH_WHITESPACE);
          })
          .appendWhereCondition(modifiers);
      executeStatement(statement);
    }
    return null;
  }
}
