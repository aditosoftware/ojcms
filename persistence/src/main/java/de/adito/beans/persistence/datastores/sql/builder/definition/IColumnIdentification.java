package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.format.IStatementFormat;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * An identifier for a database column.
 *
 * @param <TYPE> the data type of the column
 * @author Simon Danner, 05.05.2018
 */
public interface IColumnIdentification<TYPE> extends IStatementFormat
{
  /**
   * The name of the database column.
   *
   * @return the column name
   */
  String getColumnName();

  /**
   * The Java data type of the column.
   *
   * @return the data type for the database column
   */
  Class<TYPE> getDataType();

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return getColumnName();
  }

  /**
   * Determines, if the column has a numeric value.
   * By default all columns with a data type, that is based on {@link Number}, are considered to have a numeric value.
   *
   * @return <tt>true</tt>, if the column value is numeric
   */
  default boolean isNumeric()
  {
    return Number.class.isAssignableFrom(getDataType());
  }

  /**
   * Creates a new column identification.
   *
   * @param pName     the name of the column
   * @param pDataType the data type of the column
   * @param <TYPE>    the generic data type of the column
   * @return the created column identification
   */
  static <TYPE> IColumnIdentification<TYPE> of(String pName, Class<TYPE> pDataType)
  {
    return of(pName, pDataType, null);
  }

  /**
   * Creates a new column identification.
   *
   * @param pName            the name of the column
   * @param pDataType        the data type of the column
   * @param pNumericResolver an optional function to determine, if the column has a numeric value
   * @param <TYPE>           the generic data type of the column
   * @return the created column identification
   */
  static <TYPE> IColumnIdentification<TYPE> of(String pName, Class<TYPE> pDataType,
                                               @Nullable BiFunction<String, Class<TYPE>, Boolean> pNumericResolver)
  {
    return new IColumnIdentification<TYPE>()
    {
      @Override
      public String getColumnName()
      {
        return pName;
      }

      @Override
      public Class<TYPE> getDataType()
      {
        return pDataType;
      }

      @Override
      public boolean isNumeric()
      {
        return pNumericResolver == null ? IColumnIdentification.super.isNumeric() : pNumericResolver.apply(pName, pDataType);
      }
    };
  }

  /**
   * Creates an array of column identifications based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pNameResolver     a function to resolve the column name from a source object
   * @param pDataTypeResolver a function to resolve the data type for the column from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column identifications
   */
  static <SOURCE> IColumnIdentification[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                     Function<SOURCE, Class> pDataTypeResolver)
  {
    return ofMultiple(pSourceCollection, pNameResolver, pDataTypeResolver, null);
  }

  /**
   * Creates an array of column identifications based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection  the collection of source objects
   * @param pNameResolver      a function to resolve the column name from a source object
   * @param pDataTypeResolver  a function to resolve the data type for the column from a source object
   * @param pIsNumericResolver an optional function to determine, if a column has a generic column value
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of column identifications
   */
  static <SOURCE> IColumnIdentification[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                     Function<SOURCE, Class> pDataTypeResolver,
                                                     @Nullable BiFunction<String, Class, Boolean> pIsNumericResolver)
  {
    return ofMultiple(pSourceCollection.stream(), pNameResolver, pDataTypeResolver, pIsNumericResolver);
  }

  /**
   * Creates an array of column identifications based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream           a stream of source objects
   * @param pNameResolver     a function to resolve the column name from a source object
   * @param pDataTypeResolver a function to resolve the data type for the column from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column identifications
   */
  static <SOURCE> IColumnIdentification[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                     Function<SOURCE, Class> pDataTypeResolver)
  {
    return ofMultiple(pStream, pNameResolver, pDataTypeResolver, null);
  }

  /**
   * Creates an array of column identifications based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream            a stream of source objects
   * @param pNameResolver      a function to resolve the column name from a source object
   * @param pDataTypeResolver  a function to resolve the data type for the column from a source object
   * @param pIsNumericResolver an optional function to determine, if a column has a generic column value
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of column identifications
   */
  static <SOURCE> IColumnIdentification[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                     Function<SOURCE, Class> pDataTypeResolver,
                                                     @Nullable BiFunction<String, Class, Boolean> pIsNumericResolver)
  {
    //noinspection unchecked
    Function<SOURCE, IColumnIdentification> mapper = pSource -> of(pNameResolver.apply(pSource), pDataTypeResolver.apply(pSource),
                                                                   (BiFunction) pIsNumericResolver);
    return pStream
        .map(mapper)
        .toArray(IColumnIdentification[]::new);
  }
}
