package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.format.IPreparedStatementFormat;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A tuple of a column identification and a data value for database statements.
 *
 * @param <TYPE> the data type of the column of this condition
 * @author Simon Danner, 28.04.2018
 */
public interface IColumnValueTuple<TYPE> extends IPreparedStatementFormat
{
  /**
   * The column identification of this tuple.
   *
   * @return a database column identification
   */
  IColumnIdentification<TYPE> getColumn();

  /**
   * The value for the column.
   */
  TYPE getValue();

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return getColumn().getColumnName().toUpperCase() + " = ?";
  }

  @Override
  default List<IColumnValueTuple<?>> getArguments(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return Collections.singletonList(this);
  }

  /**
   * Creates a new tuple.
   *
   * @param pColumnIdentification the column identification
   * @param pValue                the value for the column
   * @param <TYPE>                the data type of the value
   * @return a column value tuple based on the given values
   */
  static <TYPE> IColumnValueTuple<TYPE> of(IColumnIdentification<TYPE> pColumnIdentification, TYPE pValue)
  {
    return new IColumnValueTuple<TYPE>()
    {
      @Override
      public IColumnIdentification<TYPE> getColumn()
      {
        return pColumnIdentification;
      }

      @Override
      public TYPE getValue()
      {
        return pValue;
      }
    };
  }

  /**
   * Creates an array of column value tuples based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pColumnResolver   a function to resolve the column identification from a source object
   * @param pValueResolver    a function to resolve the value for the column from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of column value tuples
   */
  static <SOURCE> IColumnValueTuple[] ofMultiple(Collection<SOURCE> pSourceCollection, Function<SOURCE, IColumnIdentification> pColumnResolver,
                                                 Function<SOURCE, ?> pValueResolver)
  {
    return ofMultiple(pSourceCollection.stream(), pColumnResolver, pValueResolver);
  }

  /**
   * Creates an array of column value tuples based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream         a stream of source objects
   * @param pColumnResolver a function to resolve the column identification from a source object
   * @param pValueResolver  a function to resolve the value for the column from a source object
   * @param <SOURCE>        the generic type of the source objects
   * @return an array of column value tuples
   */
  static <SOURCE> IColumnValueTuple[] ofMultiple(Stream<SOURCE> pStream, Function<SOURCE, IColumnIdentification> pColumnResolver,
                                                 Function<SOURCE, ?> pValueResolver)
  {
    //noinspection unchecked
    return pStream
        .map(pSource -> of((IColumnIdentification<Object>) pColumnResolver.apply(pSource), pValueResolver.apply(pSource)))
        .toArray(IColumnValueTuple[]::new);
  }
}
