package de.adito.beans.persistence.datastores.sql.builder.result;

import de.adito.beans.persistence.datastores.sql.builder.definition.IColumnIdentification;
import de.adito.beans.persistence.datastores.sql.builder.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The result of a select statement for a single column.
 *
 * @param <TYPE> the data type of the column
 * @author Simon Danner, 26.04.2018
 */
public class SingleColumnResult<TYPE> implements Iterable<TYPE>
{
  private final IColumnIdentification<TYPE> column;
  private final Result result;

  /**
   * Creates the single column result.
   *
   * @param pColumn the column it is based on
   * @param pResult the full result of the query
   */
  public SingleColumnResult(IColumnIdentification<TYPE> pColumn, Result pResult)
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
  public OptionalNullable<TYPE> getFirst()
  {
    return result.getFirst()
        .map(pResultRow -> OptionalNullable.of(pResultRow.get(column)))
        .orElseGet(OptionalNullable::notPresent);
  }

  @NotNull
  @Override
  public Iterator<TYPE> iterator()
  {
    return new Iterator<TYPE>()
    {
      private final Iterator<ResultRow> resultIterator = result.iterator();

      @Override
      public boolean hasNext()
      {
        return resultIterator.hasNext();
      }

      @Override
      public TYPE next()
      {
        if (hasNext())
          return resultIterator.next().get(column);
        throw new NoSuchElementException();
      }
    };
  }
}
