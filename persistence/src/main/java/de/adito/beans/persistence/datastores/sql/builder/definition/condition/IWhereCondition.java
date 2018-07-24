package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.format.IPreparedStatementFormat;

import java.util.Collection;
import java.util.function.*;
import java.util.stream.*;

/**
 * A where condition.
 * It is based on a column, a value for the column and an operator between these two components.
 *
 * @param <TYPE> the data type of the value for the condition
 * @author Simon Danner, 06.06.2018
 */
public interface IWhereCondition<TYPE> extends IColumnValueTuple<TYPE>, IPreparedStatementFormat, INegatable<IWhereCondition<TYPE>>
{
  /**
   * The operator for this condition.
   * The default operator is "=".
   *
   * @return the operator for the condition
   */
  default IWhereOperator getOperator()
  {
    return IWhereOperator.isEqual();
  }

  @Override
  default String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return getColumn().getColumnName().toUpperCase() + " " + getOperator().getLiteral() + " ?";
  }

  /**
   * Negates a where condition.
   *
   * @param pCondition  the condition to negate
   * @param <TYPE>      the data type of the condition
   * @param <CONDITION> the concrete type of the condition
   * @return the negated condition
   */
  static <TYPE, CONDITION extends IWhereCondition<TYPE>> CONDITION not(CONDITION pCondition)
  {
    //noinspection unchecked
    return (CONDITION) pCondition.not();
  }

  /**
   * Creates a where condition with a "=" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> isEqual(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue);
  }

  /**
   * Creates a where condition with a "<>" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> isNotEqual(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.isNotEqual());
  }

  /**
   * Creates a where condition with a ">" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> greaterThan(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.greaterThan());
  }

  /**
   * Creates a where condition with a "<" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> lessThan(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.lessThan());
  }

  /**
   * Creates a where condition with a ">=" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> greaterThanOrEqual(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.greaterThanOrEqual());
  }

  /**
   * Creates a where condition with a "<=" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> lessThanOrEqual(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.lessThanOrEqual());
  }

  /**
   * Creates a where condition with a "BETWEEN" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> between(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.between());
  }

  /**
   * Creates a where condition with a "LIKE" operator.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the condition
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> like(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.like());
  }

  /**
   * Creates a new where condition based on given values.
   *
   * @param pColumn the column the condition is based on
   * @param pValue  the value for the column
   * @param <TYPE>  the data type of the value
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> of(IColumnIdentification<TYPE> pColumn, TYPE pValue)
  {
    return of(pColumn, pValue, IWhereOperator.isEqual());
  }

  /**
   * Creates a new where condition based on given values.
   *
   * @param pColumn   the column the condition is based on
   * @param pValue    the value for the column
   * @param pOperator the operator for the where condition
   * @param <TYPE>    the data type of the value
   * @return the where condition
   */
  static <TYPE> IWhereCondition<TYPE> of(IColumnIdentification<TYPE> pColumn, TYPE pValue, IWhereOperator pOperator)
  {
    return new ConditionImpl<>(pColumn, pValue, pOperator);
  }

  /**
   * Creates an array of where conditions based on a collection of certain source objects to resolve the properties from.
   *
   * @param pSourceCollection the collection of source objects
   * @param pColumnResolver   a function to resolve the column identification from a source object
   * @param pValueResolver    a function to resolve the value for the column from a source object
   * @param pOperatorResolver a function to resolve the where operator from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of where conditions
   */
  static <SOURCE> IWhereConditions ofMultiple(Collection<SOURCE> pSourceCollection,
                                              Function<SOURCE, IColumnIdentification> pColumnResolver,
                                              Function<SOURCE, ?> pValueResolver,
                                              Function<SOURCE, IWhereOperator> pOperatorResolver)
  {
    return ofMultiple(pSourceCollection.stream(), pColumnResolver, pValueResolver, pOperatorResolver);
  }

  /**
   * Creates an array of where conditions based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream           a stream of source objects
   * @param pColumnResolver   a function to resolve the column identification from a source object
   * @param pValueResolver    a function to resolve the value for the column from a source object
   * @param pOperatorResolver a function to resolve the where operator from a source object
   * @param <SOURCE>          the generic type of the source objects
   * @return an array of where conditions
   */
  static <SOURCE> IWhereConditions ofMultiple(Stream<SOURCE> pStream,
                                              Function<SOURCE, IColumnIdentification> pColumnResolver,
                                              Function<SOURCE, ?> pValueResolver,
                                              Function<SOURCE, IWhereOperator> pOperatorResolver)
  {

    //noinspection unchecked
    return IWhereConditions.createFromMultiple(pStream.map(pSource -> of(pColumnResolver.apply(pSource), pValueResolver.apply(pSource),
                                                                         pOperatorResolver.apply(pSource))), EConcatenationType.AND);
  }

  /**
   * Creates an array of where conditions based on a collection of certain source objects to resolve the properties from.
   *
   * @param pCollection        the collection of source objects
   * @param pColumnResolver    a function to resolve the column identification from a source object
   * @param pValueResolver     a function to resolve the value for the column from a source object
   * @param pConditionResolver a function to resolve the where condition from the column definition and the value
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of where conditions
   */
  static <SOURCE> IWhereConditions ofMultiple(Collection<SOURCE> pCollection,
                                              Function<SOURCE, IColumnIdentification> pColumnResolver,
                                              Function<SOURCE, ?> pValueResolver,
                                              BiFunction<IColumnIdentification, Object, IWhereCondition> pConditionResolver)
  {
    return ofMultiple(pCollection.stream(), pColumnResolver, pValueResolver, pConditionResolver);
  }

  /**
   * Creates an array of where conditions based on a stream of certain source objects to resolve the properties from.
   *
   * @param pStream            a stream of source objects
   * @param pColumnResolver    a function to resolve the column identification from a source object
   * @param pValueResolver     a function to resolve the value for the column from a source object
   * @param pConditionResolver a function to resolve the where condition from the column definition and the value
   * @param <SOURCE>           the generic type of the source objects
   * @return an array of where conditions
   */
  static <SOURCE> IWhereConditions ofMultiple(Stream<SOURCE> pStream,
                                              Function<SOURCE, IColumnIdentification> pColumnResolver,
                                              Function<SOURCE, ?> pValueResolver,
                                              BiFunction<IColumnIdentification, Object, IWhereCondition> pConditionResolver)
  {
    //noinspection unchecked
    return IWhereConditions.createFromMultiple(pStream.map(pSource -> pConditionResolver.apply(pColumnResolver.apply(pSource),
                                                                                               pValueResolver.apply(pSource))), EConcatenationType.AND);
  }

  /**
   * Creates a new where condition with a "IN" operator.
   *
   * @param pColumn the column identification for the condition
   * @param pValues the values, in which the value in the database should be contained
   * @param <TYPE>  the data type of the values
   * @return the created where condition
   */
  static <TYPE> IWhereCondition<TYPE> in(IColumnIdentification<TYPE> pColumn, Iterable<TYPE> pValues)
  {
    return in(pColumn, StreamSupport.stream(pValues.spliterator(), false));
  }

  /**
   * Creates a new where condition with a "IN" operator.
   *
   * @param pColumn the column identification for the condition
   * @param pValues a stream of values, in which the value in the database should be contained
   * @param <TYPE>  the data type of the values
   * @return the created where condition
   */
  static <TYPE> IWhereCondition<TYPE> in(IColumnIdentification<TYPE> pColumn, Stream<TYPE> pValues)
  {
    return in(pColumn, IColumnValueTuple::of, pValues);
  }

  /**
   * Creates a new where condition with a "IN" operator.
   *
   * @param pColumn       the column identification for the condition
   * @param pTupleCreator a function to create a column value tuple for the values for the in condition
   *                      this is mainly used for serialization, so if the application uses special tuples, they should be used here as well
   * @param pValues       the values, in which the value in the database should be contained
   * @param <TYPE>        the data type of the values
   * @return the created where condition
   */
  static <TYPE> IWhereCondition<TYPE> in(IColumnIdentification<TYPE> pColumn,
                                         BiFunction<IColumnIdentification<TYPE>, TYPE, IColumnValueTuple<TYPE>> pTupleCreator, Iterable<TYPE> pValues)
  {
    return in(pColumn, pTupleCreator, StreamSupport.stream(pValues.spliterator(), false));
  }

  /**
   * Creates a new where condition with a "IN" operator.
   *
   * @param pColumn       the column definition for the condition
   * @param pTupleCreator a function to create a column value tuple for the values for the in condition
   *                      this is mainly used for serialization, so if the application uses special tuples, they should be used here as well
   * @param pValues       a stream of values, in which the value in the database should be contained
   * @param <TYPE>        the data type of the values
   * @return the created where condition
   */
  static <TYPE> IWhereCondition<TYPE> in(IColumnIdentification<TYPE> pColumn,
                                         BiFunction<IColumnIdentification<TYPE>, TYPE, IColumnValueTuple<TYPE>> pTupleCreator, Stream<TYPE> pValues)
  {
    return new InConditionImpl<>(pColumn, pTupleCreator, pValues);
  }
}
