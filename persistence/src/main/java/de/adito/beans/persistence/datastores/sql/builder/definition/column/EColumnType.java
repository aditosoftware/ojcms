package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import java.time.*;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enumerates all possible database column types.
 * These elements can be used to create a {@link IColumnType} instance from predefined column types.
 *
 * @author Simon Danner, 28.04.2018
 */
public enum EColumnType
{
  //text
  CHAR(Character.class), STRING(String.class), BLOB(byte[].class),
  //number
  SHORT(Short.class), INT(Integer.class), LONG(Long.class), FLOAT(Float.class), DOUBLE(Double.class),
  //date
  DATE(LocalDate.class), DATETIME(LocalDateTime.class), TIME(LocalTime.class);

  private final Class javaDataType;

  /**
   * Creates a new column type.
   *
   * @param pJavaDataType the Java data type for the column type
   */
  EColumnType(Class pJavaDataType)
  {
    javaDataType = pJavaDataType;
  }

  /**
   * Creates a column type instance from this column type.
   *
   * @return a column type instance
   */
  public IColumnType create()
  {
    return new ColumnTypeImpl(this);
  }

  /**
   * The column type for a certain Java data type.
   *
   * @param pJavaDataType the Java data type to look for
   * @return an optional column type (there may be no according data type)
   */
  public static Optional<IColumnType> getByDataType(Class pJavaDataType)
  {
    return Stream.of(values())
        .filter(pColumnType -> pColumnType.javaDataType == pJavaDataType)
        .findAny()
        .map(EColumnType::create);
  }
}
