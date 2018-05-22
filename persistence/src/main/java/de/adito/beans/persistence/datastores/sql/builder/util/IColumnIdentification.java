package de.adito.beans.persistence.datastores.sql.builder.util;

/**
 * An identifier for a database column.
 *
 * @param <TYPE> the data type of the value of the column
 * @author Simon Danner, 05.05.2018
 */
public interface IColumnIdentification<TYPE>
{
  /**
   * The name of the database column.
   */
  String getColumnName();

  /**
   * The actual value of the column from a serial value.
   *
   * @param pSerial the value in a serial format
   * @return the actual value of the column
   */
  TYPE fromSerial(String pSerial);
}
