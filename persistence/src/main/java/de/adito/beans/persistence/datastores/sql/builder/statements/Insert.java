package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.IWhereOperator;
import de.adito.beans.persistence.datastores.sql.builder.format.*;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.util.*;

/**
 * An insert statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Insert extends AbstractBaseStatement<Void, Insert>
{
  private final IColumnIdentification<Integer> idColumnIdentification;
  private final List<IColumnValueTuple<?>> values = new ArrayList<>();

  /**
   * Creates the insert statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column
   */
  public Insert(IStatementExecutor<Void> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer);
    idColumnIdentification = IColumnIdentification.of(pIdColumnName, Integer.class);
  }

  /**
   * Determines the table name to perform the insertion on.
   *
   * @param pTableName the table name
   * @return the insert statement itself to enable a pipelining mechanism
   */
  public Insert into(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Adds columns and associated values to insert.
   *
   * @param pTuples a variable amount of column value tuples
   * @return the insert statement itself to enable a pipelining mechanism
   */
  public Insert values(IColumnValueTuple<?>... pTuples)
  {
    if (pTuples == null || pTuples.length == 0)
      throw new OJDatabaseException("The tupels to insert cannot be empty!");
    values.addAll(Arrays.asList(pTuples));
    return this;
  }

  /**
   * Determines an index to insert the new row.
   *
   * @param pIndex the index to insert
   * @return the insert statement itself to enable a pipelining mechanism
   */
  public Insert atIndex(int pIndex)
  {
    if (pIndex < 0)
      throw new IllegalArgumentException("The index can not be smaller than 0! index: " + pIndex);
    values.add(0, IColumnValueTuple.of(idColumnIdentification, pIndex));
    return this;
  }

  /**
   * Executes the insertion.
   */
  public void insert()
  {
    if (values.isEmpty())
      return;

    //Increment all ids after the index to insert (if set)
    if (values.get(0).getColumn() == idColumnIdentification)
      //noinspection unchecked
      builder.doUpdate(pUpdate -> pUpdate
          .adaptId(ENumericOperation.ADD, 1)
          .whereId(IWhereOperator.greaterThanOrEqual(), ((IColumnValueTuple<Integer>) values.get(0)).getValue())
          .update());
    final StatementFormatter statement = EFormatter.INSERT.create(databaseType, idColumnIdentification.getColumnName())
        .appendTableName(getTableName())
        .openBracket()
        .appendEnumeration(values.stream().map(pTuple -> pTuple.getColumn().getColumnName().toUpperCase()),
                           ESeparator.COMMA_WITH_WHITESPACE)
        .closeBracket()
        .appendConstant(EFormatConstant.VALUES)
        .openBracket()
        .appendMultipleArgumentEnumeration(values, ESeparator.COMMA_WITH_WHITESPACE)
        .closeBracket();
    executeStatement(statement);
  }
}
