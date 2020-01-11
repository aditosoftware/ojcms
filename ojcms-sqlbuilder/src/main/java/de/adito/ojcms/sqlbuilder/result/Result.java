package de.adito.ojcms.sqlbuilder.result;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.stream.*;

/**
 * The result of a select statement.
 * This result can only be used once, because it is based on an open database connection and a cursor moving through a {@link ResultSet}.
 * Either use {@link #getFirst()} or {@link #iterator()} / {@link #stream()}.
 *
 * @author Simon Danner, 26.04.2018
 */
public final class Result implements Iterable<ResultRow>
{
  private final List<IColumnIdentification<?>> selectedColumns;
  private final IValueSerializer serializer;
  private final ResultSet resultSet;
  private final IColumnIdentification<Integer> idColumnIdentification;
  private boolean used = false;

  /**
   * Creates a new result.
   *
   * @param pSelectedColumns        the selected columns of the select statement
   * @param pIdColumnIdentification the column identification of the id column
   * @param pSerializer             the value serializer
   * @param pResult                 the result set from the query
   */
  public Result(List<IColumnIdentification<?>> pSelectedColumns, IColumnIdentification<Integer> pIdColumnIdentification,
                IValueSerializer pSerializer, ResultSet pResult)
  {
    selectedColumns = pSelectedColumns;
    idColumnIdentification = pIdColumnIdentification;
    serializer = pSerializer;
    resultSet = pResult;
  }

  /**
   * The first row of the whole result.
   *
   * @return an optional result row
   */
  public Optional<ResultRow> getFirst()
  {
    try
    {
      _checkUsage();
      return resultSet.next() ?
          Optional.of(new ResultRow(selectedColumns, idColumnIdentification, serializer, resultSet)) :
          Optional.empty();
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
  }

  @NotNull
  @Override
  public Iterator<ResultRow> iterator()
  {
    _checkUsage();

    return new Iterator<ResultRow>()
    {
      private boolean lookedAhead;
      private boolean hasNext;

      @Override
      public boolean hasNext()
      {
        try
        {
          if (!lookedAhead)
          {
            hasNext = resultSet.next();
            lookedAhead = true;
          }
          return hasNext;
        }
        catch (SQLException pE)
        {
          throw new OJDatabaseException(pE);
        }
      }

      @Override
      public ResultRow next()
      {
        if (!hasNext())
          throw new NoSuchElementException();

        lookedAhead = false;
        return new ResultRow(selectedColumns, idColumnIdentification, serializer, resultSet);
      }
    };
  }

  /**
   * A stream of all result rows of this result.
   *
   * @return a stream of result rows
   */
  public Stream<ResultRow> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }

  /**
   * Checks, if the result has been used already.
   *
   * @throws IllegalStateException if the result has been used already
   */
  private void _checkUsage()
  {
    if (used)
      throw new IllegalStateException("A result can only be used once!");
    used = true;
  }
}
