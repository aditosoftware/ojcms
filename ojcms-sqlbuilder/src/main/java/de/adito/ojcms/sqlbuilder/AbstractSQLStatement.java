package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.format.StatementFormatter;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.IStatement;
import de.adito.ojcms.utils.StringUtility;

import java.io.IOException;

/**
 * Abstract base class for every database statement.
 * It mainly provides a method the execute the statements finally.
 * For this purpose a {@link IStatementExecutor} is necessary to send statements to the database.
 * Furthermore the table name to execute the statements at and the database platform are also stored here.
 *
 * @param <RESULT>    the generic result type of the executor (e.g. {@link java.sql.ResultSet}
 * @param <STATEMENT> the final concrete type of this statement
 * @author Simon Danner, 26.04.2018
 */
public abstract class AbstractSQLStatement<RESULT, STATEMENT extends AbstractSQLStatement<RESULT, STATEMENT>> implements IStatement
{
  private final IStatementExecutor<RESULT> executor;
  protected final AbstractSQLBuilder builder;
  protected final IDatabasePlatform databasePlatform;
  protected final IValueSerializer serializer;
  protected final IColumnIdentification<Long> idColumnIdentification;
  private String tableName;

  /**
   * Creates the base statement.
   *
   * @param pExecutor     the executor for the statements
   * @param pBuilder      the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform     the database platform used for this statement
   * @param pSerializer   the value serializer
   * @param pIdColumnName the name of the global id column
   */
  protected AbstractSQLStatement(IStatementExecutor<RESULT> pExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                                 IValueSerializer pSerializer, String pIdColumnName)
  {
    executor = pExecutor;
    builder = pBuilder;
    databasePlatform = pPlatform;
    serializer = pSerializer;
    idColumnIdentification = IColumnIdentification.of(pIdColumnName.toUpperCase(), Long.class);
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
    return StringUtility.requireNotEmpty(tableName, "table name");
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

  /**
   * Determines, if the table the statement is based on has an id column.
   *
   * @return <tt>true</tt> if the id column is present
   */
  protected boolean isIdColumnPresent()
  {
    return builder.hasColumn(getTableName(), idColumnIdentification.getColumnName());
  }

  @Override
  public void close() throws IOException
  {
    executor.close();
  }
}
