package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.*;
import de.adito.beans.persistence.datastores.sql.builder.format.*;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static de.adito.beans.persistence.datastores.sql.builder.definition.condition.IWhereOperator.*;

/**
 * A delete statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Delete extends AbstractSQLStatement<WhereModifiers, Boolean, Boolean, Delete>
{
  private final IStatementExecutor<ResultSet> idUpdater;
  private final String idColumnName;

  /**
   * Creates a new delete statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdUpdater         the statement executor to update the id column, when deleting rows
   * @param pIdColumnName      the name of the id column
   */
  public Delete(IStatementExecutor<Boolean> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, IStatementExecutor<ResultSet> pIdUpdater, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, new WhereModifiers());
    idColumnName = pIdColumnName;
    idUpdater = pIdUpdater;
  }

  /**
   * Performs the deletion.
   *
   * @return <tt>true</tt>, if the deletion was successful
   */
  public boolean delete()
  {
    return doQuery();
  }

  @Override
  public void close() throws IOException
  {
    super.close();
    idUpdater.close();
  }

  @Override
  protected Boolean doQuery()
  {
    final List<Integer> idsToDelete = _getIdsToDelete();
    final StatementFormatter statement = EFormatter.DELETE.create(databaseType, idColumnName)
        .appendTableName(getTableName())
        .appendWhereCondition(modifiers);
    Boolean result = executeStatement(statement);
    //update row ids
    if (!idsToDelete.isEmpty())
    {
      //update all ranges between two ids to delete
      for (int i = 0; i < idsToDelete.size() - 1; i++)
      {
        final int offset = i;
        builder.doUpdate(pUpdate -> pUpdate
            .adaptId(ENumericOperation.SUBTRACT, offset + 1)
            .whereId(IWhereConditionsForId.create(greaterThan(), idsToDelete.get(offset))
                         .and(lessThan(), idsToDelete.get(offset + 1)))
            .update());
      }
      //update all rows after the last id to delete
      builder.doUpdate(pUpdate -> pUpdate
          .adaptId(ENumericOperation.SUBTRACT, 1)
          .whereId(greaterThan(), idsToDelete.get(idsToDelete.size() - 1))
          .update());
    }
    return result;
  }

  /**
   * The ids of the rows, that will be deleted through this delete statement.
   * Might be empty, if there's either no id column or no affected row by the statement.
   *
   * @return a list of all affected row ids of the delete statement
   */
  private List<Integer> _getIdsToDelete()
  {
    List<Integer> idsToDelete = new ArrayList<>();
    final StatementFormatter formatter = EFormatter.SELECT.create(databaseType, idColumnName)
        .appendConstant(EFormatConstant.STAR)
        .appendTableName(getTableName())
        .appendWhereCondition(modifiers);
    ResultSet result = idUpdater.executeStatement(formatter.getStatement(), formatter.getSerialArguments(serializer));
    try
    {
      int idColumnIndex = _idColumnIndex(result);
      if (idColumnIndex == -1)
        return idsToDelete;
      while (result.next())
        idsToDelete.add(result.getInt(idColumnIndex));
      return idsToDelete;
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
  }

  /**
   * The column index of the id column of a database {@link ResultSet}.
   *
   * @param pResult the result set to find the id column
   * @return the column index of the id column of the statement result
   */
  private Integer _idColumnIndex(ResultSet pResult) throws SQLException
  {
    ResultSetMetaData metaData = pResult.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++)
      if (metaData.getColumnName(i).equals(idColumnName))
        return i;
    return -1;
  }
}
