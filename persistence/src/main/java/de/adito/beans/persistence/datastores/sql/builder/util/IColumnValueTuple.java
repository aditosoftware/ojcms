package de.adito.beans.persistence.datastores.sql.builder.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A tuple of a column definition and a data value for database statements.
 *
 * @param <TYPE> the data type of the column of this condition
 * @author Simon Danner, 28.04.2018
 */
public interface IColumnValueTuple<TYPE>
{
  /**
   * The column definition.
   */
  IColumnDefinition getColumnDefinition();

  /**
   * The value for the column.
   */
  TYPE getValue();

  /**
   * The tuple's value in its serial format.
   * Per default it uses the result of {@link Object#toString()}.
   *
   * @return the value in a serial string format
   */
  default String toSerial()
  {
    return Objects.toString(getValue());
  }

  /**
   * The value as string to use for database statements.
   * For number values the string will be surrounded by quotes.
   * The value will be put in in its serial format.
   *
   * @return the value in a string format for SQL statements (with quotes for numbers)
   */
  default String valueToStatementString()
  {
    String serialValue = toSerial();
    return getColumnDefinition().getColumnType().isNumeric() ? serialValue : "'" + serialValue + "'";
  }

  /**
   * Creates a new tuple.
   *
   * @param pColumnDefinition the column definition
   * @param pValue            the value for the column
   * @param <TYPE>            the data type of the value
   * @return a column value tuple based on the given values
   */
  static <TYPE> IColumnValueTuple<TYPE> of(IColumnDefinition pColumnDefinition, TYPE pValue)
  {
    return of(pColumnDefinition, pValue, null);
  }

  /**
   * Creates a new tuple.
   *
   * @param pColumnDefinition the column definition
   * @param pValue            the value for the column
   * @param pSerializer       a function to serialize the tuple's value
   * @param <TYPE>            the data type of the value
   * @return a column value tuple based on the given values
   */
  static <TYPE> IColumnValueTuple<TYPE> of(IColumnDefinition pColumnDefinition, TYPE pValue, @Nullable Function<TYPE, String> pSerializer)
  {
    return new IColumnValueTuple<TYPE>()
    {
      @Override
      public IColumnDefinition getColumnDefinition()
      {
        return pColumnDefinition;
      }

      @Override
      public TYPE getValue()
      {
        return pValue;
      }

      @Override
      public String toSerial()
      {
        return pSerializer == null ? IColumnValueTuple.super.toSerial() : pSerializer.apply(getValue());
      }
    };
  }

  /**
   * Creates an array of column value tuples based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pColumnResolver   a function to resolve the column definition from a source object
   * @param pValueResolver    a function to resolve the value for the column from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @param <TYPE>            the generic data type of the values
   * @return an array of column value tuples
   */
  static <SOURCE, TYPE> IColumnValueTuple<TYPE>[] of(Collection<SOURCE> pSourceCollection, Function<SOURCE, IColumnDefinition> pColumnResolver,
                                                     Function<SOURCE, TYPE> pValueResolver)
  {
    return of(pSourceCollection, pColumnResolver, pValueResolver, null);
  }

  /**
   * Creates an array of column value tuples based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pColumnResolver   a function to resolve the column definition from a source object
   * @param pValueResolver    a function to resolve the value for the column from a source object
   * @param pSerializer       a function to serialize the tuple's value
   * @param <SOURCE>          the generic type of the source objects
   * @param <TYPE>            the generic data type of the values
   * @return an array of column value tuples
   */
  static <SOURCE, TYPE> IColumnValueTuple<TYPE>[] of(Collection<SOURCE> pSourceCollection, Function<SOURCE, IColumnDefinition> pColumnResolver,
                                                     Function<SOURCE, TYPE> pValueResolver, @Nullable Function<TYPE, String> pSerializer)
  {
    return of(pSourceCollection::stream, pColumnResolver, pValueResolver, pSerializer);
  }

  /**
   * Creates an array of column value tuples based on a collection of certain source objects to resolve the properties from.
   *
   * @param pStreamSupplier the collection of source objects
   * @param pColumnResolver a function to resolve the column definition from a source object
   * @param pValueResolver  a function to resolve the value for the column from a source object
   * @param <SOURCE>        the generic type of the source objects
   * @param <TYPE>          the generic data type of the values
   * @return an array of column value tuples
   */
  static <SOURCE, TYPE> IColumnValueTuple<TYPE>[] of(Supplier<Stream<SOURCE>> pStreamSupplier, Function<SOURCE, IColumnDefinition> pColumnResolver,
                                                     Function<SOURCE, TYPE> pValueResolver)
  {
    return of(pStreamSupplier, pColumnResolver, pValueResolver, null);
  }

  /**
   * Creates an array of column value tuples based on a collection of certain source objects to resolve the properties from.
   *
   * @param pStreamSupplier the collection of source objects
   * @param pColumnResolver a function to resolve the column definition from a source object
   * @param pValueResolver  a function to resolve the value for the column from a source object
   * @param pSerializer     a function to serialize the tuple's value
   * @param <SOURCE>        the generic type of the source objects
   * @param <TYPE>          the generic data type of the values
   * @return an array of column value tuples
   */
  static <SOURCE, TYPE> IColumnValueTuple<TYPE>[] of(Supplier<Stream<SOURCE>> pStreamSupplier, Function<SOURCE, IColumnDefinition> pColumnResolver,
                                                     Function<SOURCE, TYPE> pValueResolver, @Nullable Function<TYPE, String> pSerializer)
  {
    //noinspection unchecked
    return pStreamSupplier.get()
        .map(pSource -> of(pColumnResolver.apply(pSource), pValueResolver.apply(pSource), pSerializer))
        .toArray(IColumnValueTuple[]::new);
  }
}
