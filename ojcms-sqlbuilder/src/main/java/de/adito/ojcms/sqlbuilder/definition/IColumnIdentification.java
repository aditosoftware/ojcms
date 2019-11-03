package de.adito.ojcms.sqlbuilder.definition;

import de.adito.ojcms.sqlbuilder.format.IStatementFormat;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * An identifier for a database column.
 *
 * @param <VALUE> the data type of the column
 * @author Simon Danner, 05.05.2018
 */
public interface IColumnIdentification<VALUE> extends IStatementFormat
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
  Class<VALUE> getDataType();

  @Override
  default String toStatementFormat(IDatabasePlatform pPlatform, String pIdColumnName)
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
   * @param <VALUE>   the generic data type of the column
   * @return the created column identification
   */
  static <VALUE> IColumnIdentification<VALUE> of(String pName, Class<VALUE> pDataType)
  {
    return of(pName, pDataType, null);
  }

  /**
   * Creates a new column identification.
   *
   * @param pName              the name of the column
   * @param pDataType          the data type of the column
   * @param pNumericDeterminer an optional predicate to determine, if the column has a numeric value
   * @param <VALUE>            the generic data type of the column
   * @return the created column identification
   */
  static <VALUE> IColumnIdentification<VALUE> of(String pName, Class<VALUE> pDataType,
                                                 @Nullable BiPredicate<String, Class<VALUE>> pNumericDeterminer)
  {
    return new IColumnIdentification<VALUE>()
    {
      @Override
      public String getColumnName()
      {
        return pName;
      }

      @Override
      public Class<VALUE> getDataType()
      {
        return pDataType;
      }

      @Override
      public boolean isNumeric()
      {
        return pNumericDeterminer == null ? IColumnIdentification.super.isNumeric() : pNumericDeterminer.test(pName, pDataType);
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
   * @param pNumericDeterminer an optional predicate to determine, if the column has a numeric value
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of column identifications
   */
  static <SOURCE> IColumnIdentification[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, String> pNameResolver,
                                                     Function<SOURCE, Class> pDataTypeResolver,
                                                     @Nullable BiPredicate<String, Class> pNumericDeterminer)
  {
    return ofMultiple(pSourceCollection.stream(), pNameResolver, pDataTypeResolver, pNumericDeterminer);
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
   * @param pNumericDeterminer an optional predicate to determine, if the column has a numeric value
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of column identifications
   */
  static <SOURCE> IColumnIdentification[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, String> pNameResolver,
                                                     Function<SOURCE, Class> pDataTypeResolver,
                                                     @Nullable BiPredicate<String, Class> pNumericDeterminer)
  {
    //noinspection unchecked
    Function<SOURCE, IColumnIdentification> mapper = pSource -> of(pNameResolver.apply(pSource), pDataTypeResolver.apply(pSource),
                                                                   (BiPredicate) pNumericDeterminer);
    return pStream
        .map(mapper)
        .toArray(IColumnIdentification[]::new);
  }
}
