package de.adito.ojcms.sqlbuilder.statements.types;

import de.adito.ojcms.sqlbuilder.AbstractSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.condition.WhereModifiers;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.format.StatementFormatter;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.AbstractConditionStatement;

import static de.adito.ojcms.sqlbuilder.format.EFormatter.DELETE;

/**
 * A delete statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Delete extends AbstractConditionStatement<WhereModifiers, Boolean, Boolean, Delete>
{
  /**
   * Creates a new delete statement.
   *
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform          the database platform used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the id column name
   */
  public Delete(IStatementExecutor<Boolean> pStatementExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pPlatform, pSerializer, new WhereModifiers(), pIdColumnName);
  }

  /**
   * Determines on which database table this statement should be executed.
   *
   * @param pTableName the name of the database table
   * @return the statement itself to enable a pipelining mechanism
   */
  public Delete from(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Performs the deletion.
   *
   * @return <tt>true</tt>, if the deletion was successful
   */
  public boolean delete()
  {
    return doQuery();
  }

  @Override
  protected Boolean doQuery()
  {
    final StatementFormatter deleteStatement = DELETE.create(databasePlatform, idColumnIdentification.getColumnName())
        .appendTableName(getTableName())
        .appendWhereCondition(modifiers);
    return executeStatement(deleteStatement);
  }
}
