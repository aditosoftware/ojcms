package de.adito.beans.persistence.datastores.sql.builder.definition;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A definition for a database column.
 * This definition may be used to create new columns based on the properties of this configuration.
 *
 * @author Simon Danner, 28.04.2018
 */
public interface IColumnDefinition<TYPE>
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
  EColumnType getColumnType();

  /**
   * The data type of this column.
   *
   * @return a Java data type.
   */
  Class<TYPE> getDataType();

  /**
   * The size of the column.
   * This property is optional because not all column types require a size definition.
   *
   * @return the size of the column
   */
  default int getColumnSize()
  {
    return -1;
  }

  /**
   * All modifiers for this column.
   * Per default there are none.
   *
   * @return a list of column modifiers
   */
  default List<EColumnModifier> getModifiers()
  {
    return Collections.emptyList();
  }

  /**
   * Determines, if this column is a primary key column.
   *
   * @return <tt>true</tt> if it is a primary key
   */
  default boolean isPrimaryKey()
  {
    return getModifiers().contains(EColumnModifier.PRIMARY_KEY);
  }

  /**
   * The column definition database string based on the properties of this instance.
   *
   * @param pDatabaseType the database type
   * @return a string definition for the database column (e.g. 'NAME varchar(255) NOT_NULL')
   */
  default String toStatementFormat(EDatabaseType pDatabaseType)
  {
    return getColumnName().toUpperCase() + " " + getColumnType().getNameByDatabaseTypeAndSize(pDatabaseType, getColumnSize()) +
        EColumnModifier.asString(getModifiers());
  }

  /**
   * Creates an instance based on given values.
   *
   * @param pColumnName the name of the database column
   * @param pColumnType the type of the database column
   * @param pDataType   the data type of the database column
   * @return the newly created column definition
   */
  static <TYPE> IColumnDefinition<TYPE> of(String pColumnName, EColumnType pColumnType, Class<TYPE> pDataType, EColumnModifier... pModifiers)
  {
    return of(pColumnName, pColumnType, pDataType, -1, pModifiers);
  }

  /**
   * Creates an instance based on given values.
   *
   * @param pColumnName the name of the database column
   * @param pColumnType the type of the database column
   * @param pDataType   the data type of the database column
   * @param pColumnSize the size of the database column
   * @return the newly created column definition
   */
  static <TYPE> IColumnDefinition<TYPE> of(String pColumnName, EColumnType pColumnType, Class<TYPE> pDataType, int pColumnSize,
                                           EColumnModifier... pModifiers)
  {
    return new IColumnDefinition<TYPE>()
    {
      @Override
      public String getColumnName()
      {
        return pColumnName;
      }

      @Override
      public EColumnType getColumnType()
      {
        return pColumnType;
      }

      @Override
      public Class<TYPE> getDataType()
      {
        return pDataType;
      }

      @Override
      public int getColumnSize()
      {
        return pColumnSize;
      }

      @Override
      public List<EColumnModifier> getModifiers()
      {
        return Arrays.asList(pModifiers);
      }
    };
  }

  /**
   * Creates an array of column definition based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param pDataTypeResolver a function to resolve the data type of the column from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver, Function<SOURCE, Class<?>> pDataTypeResolver)
  {
    return ofMultiple(pSourceCollection, pNameResolver, pTypeResolver, pDataTypeResolver, null);
  }

  /**
   * Creates an array of column definition based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param pDataTypeResolver a function to resolve the data type of the column from a source object
   * @param pSizeResolver     an optional function to resolve the column size of the definition from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver,
                                                 Function<SOURCE, Class<?>> pDataTypeResolver,
                                                 @Nullable Function<SOURCE, Integer> pSizeResolver)
  {
    return ofMultiple(pSourceCollection.stream(), pNameResolver, pTypeResolver, pDataTypeResolver, pSizeResolver);
  }

  /**
   * Creates an array of column definitions based on a collection of certain source objects to resolve the properties from.
   *
   * @param pStream           a stream of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param pDataTypeResolver a function to resolve the data type of the column from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver, Function<SOURCE, Class<?>> pDataTypeResolver)
  {
    return ofMultiple(pStream, pNameResolver, pTypeResolver, pDataTypeResolver, null);
  }

  /**
   * Creates an array of column definitions based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream           a stream of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param pDataTypeResolver a function to resolve the data type of the column from a source object
   * @param pSizeResolver     an optional function to resolve the column size of the definition from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver, Function<SOURCE, Class<?>> pDataTypeResolver,
                                                 @Nullable Function<SOURCE, Integer> pSizeResolver)
  {
    return pStream
        .map(pSource -> of(pNameResolver.apply(pSource), pTypeResolver.apply(pSource), pDataTypeResolver.apply(pSource),
                           pSizeResolver == null ? -1 : pSizeResolver.apply(pSource)))
        .toArray(IColumnDefinition[]::new);
  }
}
