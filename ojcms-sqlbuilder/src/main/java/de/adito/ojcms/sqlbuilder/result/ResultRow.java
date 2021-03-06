package de.adito.ojcms.sqlbuilder.result;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * A single row of the result of a select statement.
 * The values of the row are stored from a {@link ResultSet}, hence a result row can be used after the database connection is closed.
 *
 * @author Simon Danner, 26.04.2018
 */
public final class ResultRow
{
  private final IColumnIdentification<Long> idColumn;
  private final Map<String, Object> values;

  /**
   * Creates a new result row.
   *
   * @param pSelectedColumns the selected columns of the select statement
   * @param pIdColumn        the column identification for the id column
   * @param pSerializer      the serializer for the database values
   * @param pResultSet       the result set to build this row from (can not be stored, because the result set may be closed in the future)
   */
  public ResultRow(List<IColumnIdentification<?>> pSelectedColumns, IColumnIdentification<Long> pIdColumn, IValueSerializer pSerializer,
                   ResultSet pResultSet)
  {
    idColumn = pIdColumn;
    values = _createValueMap(pSelectedColumns, pSerializer, pResultSet);
  }

  /**
   * Determines, if the result row contains a certain column.
   *
   * @param pColumn the column identification
   * @return <tt>true</tt>, if the column is contained
   */
  public boolean hasColumn(IColumnIdentification<?> pColumn)
  {
    return values.containsKey(pColumn.getColumnName());
  }

  /**
   * The id of this result row. Only present if the table contains an id column. Otherwise a runtime exception will be thrown.
   *
   * @return the id of the result row
   */
  public long getId()
  {
    return get(idColumn);
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

    //noinspection unchecked
    return (VALUE) values.get(pColumn.getColumnName());
  }

  /**
   * Creates a map containing all values for a collection of requested columns.
   *
   * @param pColumnsToInclude the columns to include in the map
   * @return a map containing values for the requested columns
   */
  public Map<IColumnIdentification<?>, Object> toMap(Collection<IColumnIdentification<?>> pColumnsToInclude)
  {
    return toMap(pColumnsToInclude, identity());
  }

  /**
   * Creates a map containing all values for a collection of requested columns.
   * Additionally maps the columns to any desired key type.
   *
   * @param pColumnsToInclude the columns to include in the map
   * @param pColumnMapper     mapper for the columns
   * @return a map containing values for the requested columns
   */
  public <KEY, COLUMN extends IColumnIdentification<?>> Map<KEY, Object> toMap(Collection<COLUMN> pColumnsToInclude,
                                                                               Function<COLUMN, KEY> pColumnMapper)
  {
    //noinspection unchecked
    return pColumnsToInclude.stream() //
        //Allow null values
        .collect(HashMap::new, (pMap, pColumn) -> pMap.put(pColumnMapper.apply(pColumn), get((IColumnIdentification) pColumn)),
            HashMap::putAll);
  }

  /**
   * Creates the column value map for this row.
   * The mapping is by the column name to the serial value of the associated column.
   *
   * @param pResultSet the result set of the SQL statement
   * @return the column value map for this row
   */
  private static Map<String, Object> _createValueMap(List<IColumnIdentification<?>> pSelectedColumns, IValueSerializer pSerializer,
                                                     ResultSet pResultSet)
  {
    final Map<String, IColumnIdentification<?>> nameColumnMapping = pSelectedColumns.stream() //
        .collect(Collectors.toMap(pColumn -> pColumn.getColumnName().toUpperCase(), identity()));

    try
    {
      final ResultSetMetaData metadata = pResultSet.getMetaData();
      final Map<String, Object> mapping = new HashMap<>();

      for (int i = 1; i <= metadata.getColumnCount(); i++)
      {
        final String columnName = metadata.getColumnName(i);
        final IColumnIdentification<?> column = nameColumnMapping.get(columnName);

        if (column == null)
          throw new OJDatabaseException(
              "Cannot find column '" + columnName + "' under the selected columns: " + nameColumnMapping.keySet());

        final Object dataValue = pSerializer.fromSerial(column, pResultSet, i);
        mapping.put(column.getColumnName(), dataValue);
      }

      return mapping;
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
  }
}
