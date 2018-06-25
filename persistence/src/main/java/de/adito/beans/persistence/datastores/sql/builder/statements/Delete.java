package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.modifiers.WhereModifiers;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.io.IOException;
import java.sql.*;
import java.util.*;

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
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdUpdater         the statement executor to update the id column, when deleting rows
   * @param pIdColumnName      the name of the id column
   */
  public Delete(IStatementExecutor<Boolean> pStatementExecutor, EDatabaseType pDatabaseType, IValueSerializer pSerializer,
                IStatementExecutor<ResultSet> pIdUpdater, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType, pSerializer, new WhereModifiers(pSerializer, pIdColumnName));
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
    final String whereCondition = modifiers.where();
    final String id = idColumnName;
    final List<Integer> idsToDelete = _getIdsToDelete();

    Boolean result = executeStatement("DELETE FROM " + getTableName() + whereCondition);
    //update row ids
    if (!idsToDelete.isEmpty())
    {
      //update all ranges between two ids to delete
      for (int i = 0; i < idsToDelete.size() - 1; i++)
        executeStatement("UPDATE " + getTableName() + " SET " + id + " = " + id + " - " + (i + 1) +
                             " WHERE " + id + " > " + idsToDelete.get(i) + " AND " + id + " < " + idsToDelete.get(i + 1));
      //update all rows after the last id to delete
      executeStatement("UPDATE " + getTableName() + " SET " + id + " = " + id + " - 1 WHERE " +
                           id + " > " + idsToDelete.get(idsToDelete.size() - 1));
    }
    return result;
  }

  /**
   * The ids of the rows, that will be deleted throw this delete statement.
   * Might be empty, if there's either no id column or no affected row by the statement.
   *
   * @return a list of all affected row ids of the delete statement
   */
  private List<Integer> _getIdsToDelete()
  {
    return modifiers.getWhereId()
        .map(Collections::singletonList)
        .orElseGet(() -> {
          List<Integer> idsToDelete = new ArrayList<>();
          ResultSet result = idUpdater.executeStatement("SELECT * FROM " + getTableName() + modifiers.where());
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
        });
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
