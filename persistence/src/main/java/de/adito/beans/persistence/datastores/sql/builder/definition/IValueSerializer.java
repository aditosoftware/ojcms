package de.adito.beans.persistence.datastores.sql.builder.definition;

import org.jetbrains.annotations.Nullable;

/**
 * A serialization and deserialization tool for database values.
 *
 * @author Simon Danner, 10.06.2018
 */
public interface IValueSerializer
{
  /**
   * Default serializer implementation.
   */
  IValueSerializer DEFAULT = new DefaultValueSerializer();

  /**
   * Converts the value of a column value tuple to a serial format.
   *
   * @param pColumnValueTuple the column value tuple
   * @return the value in a string format (null, if no value is present)
   */
  @Nullable <TYPE> String toSerial(IColumnValueTuple<TYPE> pColumnValueTuple);

  /**
   * Converts a serial value back to its original data value.
   * The conversion is based on a {@link IColumnIdentification} to know where the value came from.
   *
   * @param pColumnIdentification the column identification for the value
   * @param pSerialValue          the serial value in a string format
   * @param <TYPE>                the generic type of the original data value
   * @return the original data value
   */
  @Nullable <TYPE> TYPE fromSerial(IColumnIdentification<TYPE> pColumnIdentification, String pSerialValue);

  /**
   * The value as string to use for database statements.
   * For non-number values the string will be surrounded by quotes.
   * The value might be null for nullable columns.
   *
   * @param pColumnValueTuple the column value tuple to serialize
   * @return the value in a string format for SQL statements (with quotes for non-numbers)
   */
  @Nullable
  default <TYPE> String serialValueToStatementString(IColumnValueTuple<TYPE> pColumnValueTuple)
  {
    String serialValue = toSerial(pColumnValueTuple);
    if (serialValue == null)
      return null;
    return pColumnValueTuple.getColumn().isNumeric() ? serialValue : "'" + serialValue + "'";
  }
}
