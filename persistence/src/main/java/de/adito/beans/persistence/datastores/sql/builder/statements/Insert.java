package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.util.function.Function;
import java.util.stream.*;

/**
 * An insert statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Insert extends AbstractBaseStatement<Void, Insert>
{
  private final String idColumnName;
  private IColumnValueTuple<?>[] values;
  private int index = -1;

  /**
   * Creates the insert statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pIdColumnName      the name of the id column
   */
  public Insert(IStatementExecutor<Void> pStatementExecutor, EDatabaseType pDatabaseType, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType);
    idColumnName = pIdColumnName;
  }

  /**
   * Determines the table name to perform the insertion on
   *
   * @param pTableName the table name
   * @return the insert statement itself to enable a pipelining mechanism
   */
  public Insert into(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Determines the columns and associated values to insert.
   *
   * @param pTuples a collection of column value tuples
   * @return the insert statement itself to enable a pipelining mechanism
   */
  public Insert values(IColumnValueTuple<?>... pTuples)
  {
    if (pTuples.length == 0)
      throw new OJDatabaseException("The tupels to insert can not be empty!");

    values = pTuples;
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

    index = pIndex;
    return this;
  }

  /**
   * Executes the insertion.
   */
  public void insert()
  {
    final String id = idColumnName;
    if (index >= 0)
      //Increment all ids after the index to insert
      executeStatement("UPDATE " + getTableName() + " SET " + id + " = " + id + "+1 WHERE " + id + ">=" + index);
    executeStatement("INSERT INTO " + getTableName() + " (" + (index >= 0 ? id + ", " : "") +
                         _enumerate(pTuple -> pTuple.getColumnDefinition().getColumnName().toUpperCase())
                         + ") VALUES (" + (index >= 0 ? index + ", " : "") + _enumerate(IColumnValueTuple::valueToStatementString) + ")");
  }

  /**
   * Enumerates certain information of the tuples as string separated by commas.
   *
   * @param pResolver a resolver function to retrieve a value based on a tuple
   * @return the concatenated string
   */
  private String _enumerate(Function<IColumnValueTuple<?>, String> pResolver)
  {
    return Stream.of(values)
        .map(pResolver)
        .collect(Collectors.joining(", "));
  }
}
