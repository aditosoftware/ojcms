package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;

import java.util.function.BiFunction;
import java.util.stream.*;

/**
 * Implementation of a "IN" where condition for database statements.
 *
 * @param <TYPE> the data type of the value of the condition
 * @author Simon Danner, 11.06.2018
 */
class InConditionImpl<TYPE> extends ConditionImpl<TYPE>
{
  /**
   * Creates a new "IN" condition.
   *
   * @param pColumn       the column definition it is based on
   * @param pTupleCreator a function to create a column value tuple for the values for the in condition
   *                      this is mainly used for serialization
   * @param pValues       a stream of values the requested value should be in
   */
  InConditionImpl(IColumnDefinition<TYPE> pColumn, BiFunction<IColumnDefinition<TYPE>, TYPE, IColumnValueTuple<TYPE>> pTupleCreator,
                  Stream<TYPE> pValues)
  {
    super(pColumn, null, new _InOperator<>(pTupleCreator, pValues));
  }

  /**
   * The operator for the "IN" condition.
   *
   * @param <TYPE> the data type of the values of the condition
   */
  private static class _InOperator<TYPE> implements IWhereOperator<TYPE>
  {
    private final BiFunction<IColumnDefinition<TYPE>, TYPE, IColumnValueTuple<TYPE>> tupleCreator;
    private final Stream<TYPE> values;

    /**
     * Creates a new in operator.
     *
     * @param pTupleCreator a function to create a column value tuple for the values for the in condition
     *                      this is mainly used for serialization
     * @param pValues       the values for the in condition
     */
    private _InOperator(BiFunction<IColumnDefinition<TYPE>, TYPE, IColumnValueTuple<TYPE>> pTupleCreator, Stream<TYPE> pValues)
    {
      tupleCreator = pTupleCreator;
      values = pValues;
    }

    @Override
    public String getLiteral()
    {
      return "IN";
    }

    @Override
    public String toConditionFormat(IWhereCondition<TYPE> pCondition, IValueSerializer pSerializer)
    {
      return pCondition.getColumnDefinition().getColumnName() + " " + getLiteral() + "(" +
          values
              .map(pValue -> pSerializer.serialValueToStatementString(tupleCreator.apply(pCondition.getColumnDefinition(), pValue)))
              .collect(Collectors.joining(", ")) + ")";
    }
  }
}
