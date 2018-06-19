package de.adito.beans.persistence.datastores.sql.builder.definition;

/**
 * An identifier for a database column.
 *
 * @author Simon Danner, 05.05.2018
 */
public interface IColumnIdentification<TYPE>
{
  /**
   * The name of the database column.
   */
  String getColumnName();

  /**
   * The Java data type of the column.
   *
   * @return the data type for the database column
   */
  Class<TYPE> getDataType();
}
