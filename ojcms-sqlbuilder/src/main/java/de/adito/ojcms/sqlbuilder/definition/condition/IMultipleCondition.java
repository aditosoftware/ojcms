package de.adito.ojcms.sqlbuilder.definition.condition;

import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.format.IPreparedStatementFormat;

import java.util.List;
import java.util.stream.*;

/**
 * A multiple condition.
 * This interface helps combine the arguments for a prepared statement.
 *
 * @param <CONDITION> the runtime type of the multiple condition
 * @author Simon Danner, 21.07.2018
 */
interface IMultipleCondition<CONDITION extends IMultipleCondition<CONDITION>>
    extends IPreparedStatementFormat, INegatable<CONDITION>, Iterable<IPreparedStatementFormat>
{
  @Override
  default List<IColumnValueTuple<?>> getArguments(String pIdColumnName)
  {
    return stream()
        .flatMap(pCondition -> pCondition.getArguments(pIdColumnName).stream())
        .collect(Collectors.toList());
  }

  /**
   * All conditions of this concatenated condition as stream.
   *
   * @return a stream of conditions (single or multiple)
   */
  default Stream<IPreparedStatementFormat> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }
}
