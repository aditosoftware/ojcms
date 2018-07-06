package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Enumerates all possible database column types.
 *
 * @author Simon Danner, 28.04.2018
 */
public enum EColumnType implements IColumnType
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
   * The column type for a certain Java data type.
   *
   * @param pJavaDataType the Java data type to look for
   * @return a column type
   * @throws OJDatabaseException, if there's no column type for this data type
   */
  public static Optional<EColumnType> getByDataType(Class pJavaDataType)
  {
    return Stream.of(values())
        .filter(pColumnType -> pColumnType.javaDataType == pJavaDataType)
        .findAny();
  }

  @Override
  public EColumnType getType()
  {
    return this;
  }

  @Override
  public IColumnType length(int pLength)
  {
    return new ColumnTypeImpl(this).length(pLength);
  }

  @Override
  public IColumnType precision(int pPrecision)
  {
    return new ColumnTypeImpl(this).precision(pPrecision);
  }

  @Override
  public IColumnType scale(int pScale)
  {
    return new ColumnTypeImpl(this).scale(pScale);
  }

  @Override
  public IColumnType primaryKey()
  {
    return new ColumnTypeImpl(this).primaryKey();
  }

  @Override
  public IColumnType foreignKey(IForeignKey pForeignKey)
  {
    return new ColumnTypeImpl(this).foreignKey(pForeignKey);
  }

  @Override
  public IColumnType modifiers(EColumnModifier... pModifiers)
  {
    return new ColumnTypeImpl(this).modifiers(pModifiers);
  }

  @NotNull
  @Override
  public Iterator<EColumnModifier> iterator()
  {
    return Collections.emptyIterator();
  }
}
