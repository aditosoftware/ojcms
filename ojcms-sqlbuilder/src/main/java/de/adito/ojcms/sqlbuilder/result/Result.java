package de.adito.ojcms.sqlbuilder.result;

import de.adito.ojcms.sqlbuilder.definition.IValueSerializer;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.stream.*;

/**
 * The result of a select statement.
 * This result can only be used once, because it is based on an open database connection
 * and a cursor, which is moved through a {@link ResultSet}.
 * Either use {@link #getFirst()} or {@link #iterator()} / {@link #stream()}.
 *
 * @author Simon Danner, 26.04.2018
 */
public final class Result implements Iterable<ResultRow>
{
  private final IValueSerializer serializer;
  private final ResultSet resultSet;
  private boolean used = false;

  /**
   * Creates a new result.
   *
   * @param pSerializer a value serializer
   * @param pResult     the result set from the query
   */
  public Result(IValueSerializer pSerializer, ResultSet pResult)
  {
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
    _checkUsage();
    try
    {
      return resultSet.next() ? Optional.of(new ResultRow(serializer, resultSet)) : Optional.empty();
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
        return new ResultRow(serializer, resultSet);
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
