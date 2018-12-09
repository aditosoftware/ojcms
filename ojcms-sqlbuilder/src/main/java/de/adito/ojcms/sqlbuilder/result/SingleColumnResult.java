package de.adito.ojcms.sqlbuilder.result;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.utils.OptionalNullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The result of a select statement for a single column.
 *
 * @param <VALUE> the data type of the column
 * @author Simon Danner, 26.04.2018
 */
public class SingleColumnResult<VALUE> implements Iterable<VALUE>
{
  private final IColumnIdentification<VALUE> column;
  private final Result result;

  /**
   * Creates the single column result.
   *
   * @param pColumn the column it is based on
   * @param pResult the full result of the query
   */
  public SingleColumnResult(IColumnIdentification<VALUE> pColumn, Result pResult)
  {
    column = pColumn;
    result = pResult;
  }

  /**
   * The value of the result from the first row.
   * The result is optional because there may not be a first row.
   *
   * @return an optional result value of the first row
   */
  public OptionalNullable<VALUE> getFirst()
  {
    return result.getFirst()
        .map(pResultRow -> OptionalNullable.of(pResultRow.get(column)))
        .orElseGet(OptionalNullable::notPresent);
  }

  @NotNull
  @Override
  public Iterator<VALUE> iterator()
  {
    return new Iterator<VALUE>()
    {
      private final Iterator<ResultRow> resultIterator = result.iterator();

      @Override
      public boolean hasNext()
      {
        return resultIterator.hasNext();
      }

      @Override
      public VALUE next()
      {
        if (hasNext())
          return resultIterator.next().get(column);
        throw new NoSuchElementException();
      }
    };
  }
}
