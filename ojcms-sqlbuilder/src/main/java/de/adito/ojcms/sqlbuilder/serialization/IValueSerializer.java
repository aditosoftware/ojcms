package de.adito.ojcms.sqlbuilder.serialization;

import de.adito.ojcms.sqlbuilder.definition.*;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;

/**
 * A serialization and deserialization tool for database values.
 *
 * @author Simon Danner, 10.06.2018
 */
public interface IValueSerializer
{
  /**
   * Converts the value of a column value tuple to a serial format.
   *
   * @param pColumnValueTuple the column value tuple
   * @return the serial value (null, if no value is present)
   */
  @Nullable <VALUE> ISerialValue toSerial(IColumnValueTuple<VALUE> pColumnValueTuple);

  /**
   * Retrieves a value from a {@link ResultSet} and converts it back to its original data value.
   *
   * @param pColumn    the column associated with the value to convert
   * @param pResultSet the SQL result set to retrieve the value from
   * @param pIndex     the index to retrieve the result from
   * @return the original data value
   */
  @Nullable <VALUE> VALUE fromSerial(IColumnIdentification<VALUE> pColumn, ResultSet pResultSet, int pIndex);
}
