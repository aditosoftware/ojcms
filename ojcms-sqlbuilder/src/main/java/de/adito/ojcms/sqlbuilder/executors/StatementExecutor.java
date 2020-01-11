package de.adito.ojcms.sqlbuilder.executors;

import de.adito.ojcms.sqlbuilder.serialization.ISerialValue;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implementation of a statement executor based on a function that will be provided with a {@link PreparedStatement}.
 * The function then should return the result of the statement.
 *
 * @param <RESULT> the generic type of the result
 * @author Simon Danner, 05.01.2020
 */
class StatementExecutor<RESULT> implements IStatementExecutor<RESULT>
{
  private final Supplier<Connection> connectionSupplier;
  private final ThrowingFunction<PreparedStatement, RESULT, SQLException> executor;
  private Connection connection;
  private PreparedStatement statement;

  /**
   * Creates the executor.
   *
   * @param pConnectionSupplier supplier for SQL connections
   * @param pExecutor           the executing function provided with a {@link PreparedStatement}
   */
  StatementExecutor(Supplier<Connection> pConnectionSupplier, ThrowingFunction<PreparedStatement, RESULT, SQLException> pExecutor)
  {
    connectionSupplier = pConnectionSupplier;
    executor = pExecutor;
  }

  @Override
  public RESULT executeStatement(String pSQLStatement, List<ISerialValue> pArgs)
  {
    connection = connectionSupplier.get();

    try
    {
      statement = connection.prepareStatement(pSQLStatement); //NOSONAR

      int argIndex = 1;
      for (ISerialValue arg : pArgs)
        arg.applyToStatement(statement, argIndex++);

      return executor.apply(statement);
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pSQLStatement, pE);
    }
  }

  @Override
  public void close() throws IOException
  {
    try
    {
      if (statement != null)
        statement.close();
      if (connection != null)
        connection.close();
    }
    catch (SQLException pE)
    {
      throw new IOException(pE);
    }
  }
}
