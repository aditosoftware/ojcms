package de.adito.ojcms.sqlbuilder.statements.types;

import de.adito.ojcms.sqlbuilder.*;
import de.adito.ojcms.sqlbuilder.definition.IColumnValueTuple;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.format.StatementFormatter;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.util.*;

import static de.adito.ojcms.sqlbuilder.definition.ENumericOperation.ADD;
import static de.adito.ojcms.sqlbuilder.definition.condition.IWhereOperator.greaterThanOrEqual;
import static de.adito.ojcms.sqlbuilder.format.EFormatConstant.VALUES;
import static de.adito.ojcms.sqlbuilder.format.EFormatter.INSERT;
import static de.adito.ojcms.sqlbuilder.format.ESeparator.COMMA_WITH_WHITESPACE;

/**
 * An insert statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Insert extends AbstractSQLStatement<Void, Insert>
{
  private final List<IColumnValueTuple<?>> values = new ArrayList<>();

  /**
   * Creates the insert statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform          the database platform used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column
   */
  public Insert(IStatementExecutor<Void> pStatementExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pPlatform, pSerializer, pIdColumnName);
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
      throw new OJDatabaseException("The tuples to insert cannot be empty!");
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
          .table(getTableName())
          .adaptId(ADD, 1)
          .whereId(greaterThanOrEqual(), ((IColumnValueTuple<Integer>) values.get(0)).getValue())
          .update());

    final StatementFormatter statement = INSERT.create(databasePlatform, idColumnIdentification.getColumnName())
        .appendTableName(getTableName())
        .openBracket()
        .appendEnumeration(values.stream().map(pTuple -> pTuple.getColumn().getColumnName().toUpperCase()),
                           COMMA_WITH_WHITESPACE)
        .closeBracket()
        .appendConstant(VALUES)
        .openBracket()
        .appendMultipleArgumentEnumeration(values, COMMA_WITH_WHITESPACE)
        .closeBracket();
    executeStatement(statement);
  }
}
