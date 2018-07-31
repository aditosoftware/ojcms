package de.adito.beans.persistence.datastores.sql.builder.definition.column;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;
import de.adito.beans.persistence.datastores.sql.builder.format.IStatementFormat;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A definition for a database column.
 * This definition may be used to create new columns based on the properties of this configuration.
 *
 * @author Simon Danner, 28.04.2018
 */
public interface IColumnDefinition extends IStatementFormat
{
  /**
   * The name of the database column.
   *
   * @return a database column name
   */
  String getColumnName();

  /**
   * The database column type.
   *
   * @return a database column type
   */
  IColumnType getColumnType();

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return getColumnName().toUpperCase() + " " + getColumnType().toStatementFormat(pDatabaseType, pIdColumnName);
  }

  /**
   * Creates a column definition from a Java data type.
   *
   * @param pType       the data type
   * @param pColumnName the name of the column
   * @param pModifier   an optional function to modify the column type
   * @return the created column definition
   */
  static IColumnDefinition forType(Class pType, String pColumnName, @Nullable Function<IColumnType, IColumnType> pModifier)
  {
    IColumnType columnType = EColumnType.getByDataType(pType)
        .orElseThrow(() -> new OJDatabaseException("No column type found for data type " + pType.getName()));
    return of(pColumnName, pModifier == null ? columnType : pModifier.apply(columnType));
  }

  /**
   * Creates an instance based on given values.
   *
   * @param pColumnName the name of the database column
   * @param pColumnType the type of the database column
   * @return the newly created column definition
   */
  static IColumnDefinition of(String pColumnName, IColumnType pColumnType)
  {
    return new IColumnDefinition()
    {
      @Override
      public String getColumnName()
      {
        return pColumnName;
      }

      @Override
      public IColumnType getColumnType()
      {
        return pColumnType;
      }
    };
  }

  /**
   * Creates an array of column definitions based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, IColumnType> pTypeResolver)
  {
    return ofMultiple(pSourceCollection.stream(), pNameResolver, pTypeResolver);
  }

  /**
   * Creates an array of column definitions based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream       a stream of source objects
   * @param pNameResolver a function to resolve the column name of the definition from a source object
   * @param pTypeResolver a function to resolve the column type of the definition from a source object
   * @param <SOURCE>      the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, IColumnType> pTypeResolver)
  {
    return pStream
        .map(pSource -> of(pNameResolver.apply(pSource), pTypeResolver.apply(pSource)))
        .toArray(IColumnDefinition[]::new);
  }
}
