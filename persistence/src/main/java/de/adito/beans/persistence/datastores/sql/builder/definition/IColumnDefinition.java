package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;
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
public interface IColumnDefinition
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
   * @return a set of column modifiers
   */
  default Set<EColumnModifier> getModifiers()
  {
    return Collections.emptySet();
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
   * Creates a default column definition for certain Java data types.
   * Example: String.class -> STRING(255)
   *
   * @param pType       the data type
   * @param pColumnName the name of the column
   * @param pModifiers  a variable amount of column modifiers
   * @return the created column definition
   */
  static IColumnDefinition forType(Class pType, String pColumnName, EColumnModifier... pModifiers)
  {
    EColumnType columnType = EColumnType.getByDataType(pType)
        .orElseThrow(() -> new OJDatabaseException("No column type found for data type " + pType.getName()));
    return of(pColumnName, columnType, columnType.getDefaultSize(), pModifiers);
  }

  /**
   * Creates an instance based on given values.
   *
   * @param pColumnName the name of the database column
   * @param pColumnType the type of the database column
   * @return the newly created column definition
   */
  static IColumnDefinition of(String pColumnName, EColumnType pColumnType, EColumnModifier... pModifiers)
  {
    return of(pColumnName, pColumnType, -1, pModifiers);
  }

  /**
   * Creates an instance based on given values.
   *
   * @param pColumnName the name of the database column
   * @param pColumnType the type of the database column
   * @param pColumnSize the size of the database column
   * @return the newly created column definition
   */
  static IColumnDefinition of(String pColumnName, EColumnType pColumnType, int pColumnSize, EColumnModifier... pModifiers)
  {
    return new ColumnDefinitionImpl(pColumnName, pColumnType, pColumnSize, pModifiers);
  }

  /**
   * Creates an array of column definition based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver)
  {
    return ofMultiple(pSourceCollection, pNameResolver, pTypeResolver, null);
  }

  /**
   * Creates an array of column definition based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pNameResolver     a function to resolve the column name of the definition from a source object
   * @param pTypeResolver     a function to resolve the column type of the definition from a source object
   * @param pSizeResolver     an optional function to resolve the column size of the definition from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver,
                                                 @Nullable Function<SOURCE, Integer> pSizeResolver)
  {
    return ofMultiple(pSourceCollection.stream(), pNameResolver, pTypeResolver, pSizeResolver);
  }

  /**
   * Creates an array of column definitions based on a collection of certain source objects to resolve the properties from.
   *
   * @param pStream       a stream of source objects
   * @param pNameResolver a function to resolve the column name of the definition from a source object
   * @param pTypeResolver a function to resolve the column type of the definition from a source object
   * @param <SOURCE>      the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver)
  {
    return ofMultiple(pStream, pNameResolver, pTypeResolver, null);
  }

  /**
   * Creates an array of column definitions based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream       a stream of source objects
   * @param pNameResolver a function to resolve the column name of the definition from a source object
   * @param pTypeResolver a function to resolve the column type of the definition from a source object
   * @param pSizeResolver an optional function to resolve the column size of the definition from a source object
   * @param <SOURCE>      the generic type of the source objects
   * @return an array of column definitions
   */
  static <SOURCE> IColumnDefinition[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                 Function<SOURCE, EColumnType> pTypeResolver,
                                                 @Nullable Function<SOURCE, Integer> pSizeResolver)
  {
    return pStream
        .map(pSource -> of(pNameResolver.apply(pSource), pTypeResolver.apply(pSource),
                           pSizeResolver == null ? -1 : pSizeResolver.apply(pSource)))
        .toArray(IColumnDefinition[]::new);
  }
}
