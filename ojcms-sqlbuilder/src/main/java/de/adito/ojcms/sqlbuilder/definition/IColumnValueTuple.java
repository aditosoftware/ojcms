package de.adito.ojcms.sqlbuilder.definition;

import de.adito.ojcms.sqlbuilder.format.IPreparedStatementFormat;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A tuple of a column identification and a data value for database statements.
 *
 * @param <VALUE> the data type of the column of this condition
 * @author Simon Danner, 28.04.2018
 */
public interface IColumnValueTuple<VALUE> extends IPreparedStatementFormat
{
  /**
   * The column identification of this tuple.
   *
   * @return a database column identification
   */
  IColumnIdentification<VALUE> getColumn();

  /**
   * The value for the column.
   *
   * @return the value
   */
  VALUE getValue();

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
   * @param <VALUE>               the data type of the value
   * @return a column value tuple based on the given values
   */
  static <VALUE> IColumnValueTuple<VALUE> of(IColumnIdentification<VALUE> pColumnIdentification, VALUE pValue)
  {
    return new IColumnValueTuple<VALUE>()
    {
      @Override
      public IColumnIdentification<VALUE> getColumn()
      {
        return pColumnIdentification;
      }

      @Override
      public VALUE getValue()
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
