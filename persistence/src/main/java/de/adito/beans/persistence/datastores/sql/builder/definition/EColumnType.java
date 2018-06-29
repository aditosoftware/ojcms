package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.time.*;
import java.util.stream.Stream;

/**
 * Enumerates all possible database column types.
 *
 * @author Simon Danner, 28.04.2018
 */
public enum EColumnType
{
  //text
  SINGLE_CHAR(Character.class), STRING(String.class, 255), BLOB(byte[].class),
  //number
  SHORT(Short.class), INT(Integer.class), LONG(Long.class), FLOAT(Float.class), DOUBLE(Double.class),
  //date
  DATE(LocalDate.class), DATETIME(LocalDateTime.class), TIME(LocalTime.class);

  private final Class javaDataType;
  private final int defaultSize;

  /**
   * Creates a new column type.
   *
   * @param pJavaDataType the Java data type for the column type
   */
  EColumnType(Class pJavaDataType)
  {
    this(pJavaDataType, -1);
  }

  /**
   * Creates a new column type.
   *
   * @param pJavaDataType the Java data type for the column type
   * @param pDefaultSize  the default size for this type (-1, if this type has no size)
   */
  EColumnType(Class pJavaDataType, int pDefaultSize)
  {
    javaDataType = pJavaDataType;
    defaultSize = pDefaultSize;
  }

  /**
   * The default size for this column type.
   * -1, if this type has no size.
   *
   * @return the default size for the column type
   */
  public int getDefaultSize()
  {
    return defaultSize;
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

  /**
   * The column type for a certain Java data type.
   *
   * @param pJavaDataType the Java data type to look for
   * @return a column type
   * @throws OJDatabaseException, if there's no column type for this data type
   */
  public static EColumnType getByDataType(Class pJavaDataType)
  {
    return Stream.of(values())
        .filter(pColumnType -> pColumnType.javaDataType == pJavaDataType)
        .findAny()
        .orElseThrow(() -> new OJDatabaseException("No column type found for data type " + pJavaDataType.getName()));
  }
}
