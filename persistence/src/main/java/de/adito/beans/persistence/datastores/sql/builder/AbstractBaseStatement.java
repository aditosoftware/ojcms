package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.format.StatementFormatter;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.io.IOException;

/**
 * Abstract base class for every database statement.
 * It mainly provides a method the execute the statements finally.
 * For this purpose a {@link IStatementExecutor} is necessary to send statements to the database.
 * Furthermore the table name to execute the statements at and the database type are also stored here.
 *
 * @param <RESULT>    the generic result type of the executor (e.g. {@link java.sql.ResultSet}
 * @param <STATEMENT> the final concrete type of this statement
 * @author Simon Danner, 26.04.2018
 */
public abstract class AbstractBaseStatement<RESULT, STATEMENT extends AbstractBaseStatement<RESULT, STATEMENT>> implements IStatement
{
  private final IStatementExecutor<RESULT> executor;
  protected final AbstractSQLBuilder builder;
  protected final EDatabaseType databaseType;
  protected final IValueSerializer serializer;
  private String tableName;

  /**
   * Creates the base statement.
   *
   * @param pExecutor     the executor for the statements
   * @param pBuilder      the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType the database type used for this statement
   * @param pSerializer   the value serializer
   */
  protected AbstractBaseStatement(IStatementExecutor<RESULT> pExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                                  IValueSerializer pSerializer)
  {
    executor = pExecutor;
    builder = pBuilder;
    databaseType = pDatabaseType;
    serializer = pSerializer;
  }

  /**
   * Executes a SQL statement.
   *
   * @param pFormat the statement defined through a formatter
   * @return the result of the execution
   */
  protected RESULT executeStatement(StatementFormatter pFormat)
  {
    return executor.executeStatement(pFormat.getStatement(), pFormat.getSerialArguments(serializer));
  }

  /**
   * The table name to use for this statement.
   *
   * @return a table name of the database
   */
  protected String getTableName()
  {
    if (tableName == null || tableName.isEmpty())
      throw new OJDatabaseException("A table name must be set to execute this SQL-statement!");
    return tableName;
  }

  /**
   * Sets the table name for this statement.
   *
   * @param pTableName the table name
   * @return the statement itself
   */
  protected STATEMENT setTableName(String pTableName)
  {
    tableName = pTableName.toUpperCase();
    //noinspection unchecked
    return (STATEMENT) this;
  }

  @Override
  public void close() throws IOException
  {
    executor.close();
  }
}
