package de.adito.ojcms.sqlbuilder.result;

import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.sql.*;
import java.util.*;

/**
 * A single row of the result of a select statement.
 * The values of the row are stored from a {@link ResultSet}, hence a result row can be used after the database connection is closed.
 *
 * @author Simon Danner, 26.04.2018
 */
public final class ResultRow
{
  private final IValueSerializer serializer;
  private final Map<String, String> values;

  /**
   * Creates a new result row.
   *
   * @param pSerializer the serializer for the database values
   * @param pResultSet  the result set to build this row from (can not be stored, because the result set may be closed in the future)
   */
  public ResultRow(IValueSerializer pSerializer, ResultSet pResultSet)
  {
    serializer = pSerializer;
    values = _createValueMap(pResultSet);
  }

  /**
   * Determines, if the result row contains a certain column.
   *
   * @param pColumn the column identification
   * @return <tt>true</tt>, if the column is contained
   */
  public boolean hasColumn(IColumnIdentification<?> pColumn)
  {
    return values.containsKey(pColumn.getColumnName().toUpperCase());
  }

  /**
   * The value of this row for a certain column.
   *
   * @param pColumn the column identification
   * @param <VALUE> the column's data type
   * @return the value
   * @throws OJDatabaseException if the column is not present within the result row
   */
  public <VALUE> VALUE get(IColumnIdentification<VALUE> pColumn)
  {
    if (!hasColumn(pColumn))
      throw new OJDatabaseException("The column '" + pColumn.getColumnName() + "' is not present within the result row!");

    final String serialValue = values.get(pColumn.getColumnName().toUpperCase());
    return serializer.fromSerial(pColumn, serialValue);
  }

  /**
   * Creates the column value map for this row.
   * The mapping is from the column name to the serial value of the associated column.
   *
   * @param pResultSet the result set of the SQL statement
   * @return the column value map for this row
   */
  private static Map<String, String> _createValueMap(ResultSet pResultSet)
  {
    try
    {
      final ResultSetMetaData metadata = pResultSet.getMetaData();
      final Map<String, String> mapping = new HashMap<>();
      for (int i = 1; i <= metadata.getColumnCount(); i++)
        mapping.put(metadata.getColumnName(i).toUpperCase(), pResultSet.getString(i));
      return mapping;
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
  }
}
