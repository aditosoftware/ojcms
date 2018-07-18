package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.modifiers.WhereModifiers;

import java.util.*;
import java.util.stream.*;

/**
 * An update statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Update extends AbstractSQLStatement<WhereModifiers, Void, Void, Update>
{
  private final List<IColumnValueTuple<?>> changes = new ArrayList<>();
  private final List<INumericValueAdaption<?>> updateOldValues = new ArrayList<>();
  private final IColumnIdentification<Integer> idColumnIdentification;

  /**
   * Creates a new update statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Update(IStatementExecutor<Void> pStatementExecutor, EDatabaseType pDatabaseType, IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType, pSerializer, new WhereModifiers(pSerializer, pIdColumnName));
    idColumnIdentification = IColumnIdentification.of(pIdColumnName, Integer.class);
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
   * @param pNumber    a number to apply on the numeric operation ant the old id
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
    if (changes.size() > 0 || updateOldValues.size() > 0)
      executeStatement("UPDATE " + getTableName() + " SET " + _changes() + modifiers.where());
    return null;
  }

  /**
   * Enumerates the changes as one string in this format: 'COLUMN_NAME = VALUE'.
   *
   * @return the concatenated string
   */
  private String _changes()
  {
    return Stream.concat(changes.stream()
                             .map(pChange -> pChange.toStatementFormat(serializer)),
                         updateOldValues.stream()
                             .map(INumericValueAdaption::toStatementFormat))
        .collect(Collectors.joining(", "));
  }
}
