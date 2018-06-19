package de.adito.beans.persistence.datastores.sql.builder.definition;

/**
 * Enumerates all possible database column types.
 *
 * @author Simon Danner, 28.04.2018
 */
public enum EColumnType
{
  //text
  CHAR, VARCHAR, TEXT, BLOB,
  //number
  SHORT(true), INT(true), BIGINT(true), FLOAT(true), DOUBLE(true),
  //date
  DATE, DATETIME, TIMESTAMP, TIME;

  private final boolean isNumeric;

  /**
   * Creates a new column type with a non numeric data value type.
   */
  EColumnType()
  {
    this(false);
  }

  /**
   * Creates a new column type.
   *
   * @param pIsNumeric <tt>true</tt>, if the data value of this column is numeric
   */
  EColumnType(boolean pIsNumeric)
  {
    isNumeric = pIsNumeric;
  }

  /**
   * Determines, if this column type has a numeric value.
   *
   * @return <tt>true</tt>, if the column has a numeric value type
   */
  public boolean isNumeric()
  {
    return isNumeric;
  }

  /**
   * The name of the column type for a certain database system.
   *
   * @param pDatabaseType the database type
   * @return the name of the column for the given database system
   */
  public String getNameByDatabaseType(EDatabaseType pDatabaseType)
  {
    return pDatabaseType.getColumnDefinition(this);
  }

  /**
   * The name of the column type with a size information for a certain database system.
   *
   * @param pDatabaseType the database type
   * @param pSize         the size of the column
   * @return the name of the column with a size information for the given database system
   */
  public String getNameByDatabaseTypeAndSize(EDatabaseType pDatabaseType, int pSize)
  {
    return pDatabaseType.getColumnDefinition(this, pSize);
  }
}
