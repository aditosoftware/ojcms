package de.adito.beans.persistence.datastores.sql.builder.result;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.sql.*;
import java.util.*;

/**
 * Defines a single row of the result of a select statement.
 * The values of the row are stored from a {@link ResultSet}, hence a result row can be used after the database connection is closed.
 *
 * @author Simon Danner, 26.04.2018
 */
public class ResultRow
{
  private final IValueSerializer serializer;
  private final int index; //-1 if not available
  private final Map<String, String> values;

  /**
   * Creates a new result row.
   *
   * @param pSerializer   the serializer for the database values
   * @param pResultSet    the result set to build this row from (can not be stored, because the result set may be closed in the future)
   * @param pIdColumnName the name of the id column
   */
  public ResultRow(IValueSerializer pSerializer, ResultSet pResultSet, String pIdColumnName)
  {
    serializer = pSerializer;
    index = _getIdIfAvailable(pResultSet, pIdColumnName);
    values = _createValueMap(pResultSet);
  }

  /**
   * Determines, if the result row contains a certain column.
   *
   * @param pColumn the column identification
   * @return <tt>true</tt>, if the column is contained
   */
  public boolean hasColumn(IColumnIdentification pColumn)
  {
    return values.containsKey(pColumn.getColumnName().toUpperCase());
  }

  /**
   * The value of this row for a certain column.
   *
   * @param pColumn the column identification
   * @param <TYPE>  the column's data type
   * @return the value
   * @throws OJDatabaseException if the column is not present within the result row
   */
  public <TYPE> TYPE get(IColumnIdentification<TYPE> pColumn)
  {
    if (!hasColumn(pColumn))
      throw new OJDatabaseException("The column '" + pColumn.getColumnName() + "' is not present within the result row!");

    String serialValue = values.get(pColumn.getColumnName().toUpperCase());
    return serializer.fromSerial(pColumn, serialValue);
  }

  /**
   * The id/index of the row, if available.
   *
   * @return the id of this result row
   * @throws OJDatabaseException if not available
   */
  public Integer getIdIfAvailable()
  {
    if (index < 0)
      throw new OJDatabaseException("An id column is not available in this result row!");
    return index;
  }

  /**
   * The index of this row, -1 if not available.
   *
   * @param pResultSet    the result set of the SQL statement
   * @param pIdColumnName the name of the id column
   * @return the index of the row, or -1 if not available
   */
  private int _getIdIfAvailable(ResultSet pResultSet, String pIdColumnName)
  {
    try
    {
      return pResultSet.getInt(pIdColumnName);
    }
    catch (SQLException pE)
    {
      return -1;
    }
  }

  /**
   * Creates the column value map for this row.
   * The mapping is from the column name to the serial value of the associated column.
   *
   * @param pResultSet the result set of the SQL statement
   * @return the column value map for this row
   */
  private Map<String, String> _createValueMap(ResultSet pResultSet)
  {
    try
    {
      ResultSetMetaData metadata = pResultSet.getMetaData();
      Map<String, String> mapping = new HashMap<>();
      for (int i = index == -1 ? 1 : 2; i <= metadata.getColumnCount(); i++)
        mapping.put(metadata.getColumnName(i).toUpperCase(), pResultSet.getString(i));
      return mapping;
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
  }
}
