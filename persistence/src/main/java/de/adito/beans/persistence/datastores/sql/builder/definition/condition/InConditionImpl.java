package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.format.*;

import java.util.*;
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
  private final List<IColumnValueTuple<TYPE>> values;

  /**
   * Creates a new "IN" condition.
   *
   * @param pColumn       the column identification it is based on
   * @param pTupleCreator a function to create a column value tuple for the values for the in condition
   *                      this is mainly used for serialization
   * @param pValues       a stream of values the requested value should be in
   */
  InConditionImpl(IColumnIdentification<TYPE> pColumn, BiFunction<IColumnIdentification<TYPE>, TYPE, IColumnValueTuple<TYPE>> pTupleCreator,
                  Stream<TYPE> pValues)
  {
    super(pColumn, null, () -> ""); //value and operator do not matter
    values = pValues
        .map(pValue -> pTupleCreator.apply(pColumn, pValue))
        .collect(Collectors.toList());
    if (values.isEmpty())
      throw new RuntimeException("The elements of an 'IN'-condition cannot be empty!");
  }

  @Override
  public String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return getColumn().getColumnName().toUpperCase() + " " + EFormatConstant.IN.toStatementFormat(
        StatementFormatter.join(IntStream.range(0, values.size()).mapToObj(pIndex -> "?"), ESeparator.COMMA_WITH_WHITESPACE));
  }

  @Override
  public List<IColumnValueTuple<?>> getArguments(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return Collections.unmodifiableList(values);
  }
}
