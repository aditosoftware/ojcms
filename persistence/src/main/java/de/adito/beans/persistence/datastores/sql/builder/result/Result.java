package de.adito.beans.persistence.datastores.sql.builder.result;

import de.adito.beans.persistence.datastores.sql.builder.definition.IValueSerializer;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;

/**
 * The result of a select statement.
 * This result can only be used once, because it is based on an open database connection
 * and a cursor, which is moved through a {@link ResultSet}.
 * Either use {@link #getFirst()} or {@link #iterator()}.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Result implements Iterable<ResultRow>
{
  private final IValueSerializer serializer;
  private final ResultSet resultSet;
  private final String idColumnName;
  private boolean used = false;

  /**
   * Creates a new result.
   *
   * @param pSerializer   a value serializer
   * @param pResult       the result set from the query
   * @param pIdColumnName the name of the id column
   */
  public Result(IValueSerializer pSerializer, ResultSet pResult, String pIdColumnName)
  {
    serializer = pSerializer;
    resultSet = pResult;
    idColumnName = pIdColumnName;
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
      return resultSet.next() ? Optional.of(new ResultRow(serializer, resultSet, idColumnName)) : Optional.empty();
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
      @Override
      public boolean hasNext()
      {
        try
        {
          return !resultSet.isAfterLast();
        }
        catch (SQLException pE)
        {
          throw new OJDatabaseException(pE);
        }
      }

      @Override
      public ResultRow next()
      {
        if (hasNext())
        {
          try
          {
            resultSet.next();
            return new ResultRow(serializer, resultSet, idColumnName);
          }
          catch (SQLException pE)
          {
            throw new OJDatabaseException(pE);
          }
        }
        throw new NoSuchElementException();
      }
    };
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
