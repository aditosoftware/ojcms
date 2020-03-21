package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.platform.connection.IDatabaseConnectionSupplier;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.types.Create;
import de.adito.ojcms.utils.StringUtility;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Implementation of the SQL builder for single tables.
 *
 * @author Simon Danner, 15.05.2018
 */
final class OJSQLBuilderForTableImpl extends AbstractSQLBuilder implements OJSQLBuilderForTable
{
  private final String tableName;

  /**
   * Creates a new builder.
   *
   * @param pPlatform            the database platform to use for this builder
   * @param pConnectionSupplier  the database platform based connection supplier
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pSerializer          the value serializer
   * @param pTableName           the name of the table to use for this builder
   * @param pIdColumnName        a global id column name for this builder instance
   */
  OJSQLBuilderForTableImpl(IDatabasePlatform pPlatform, IDatabaseConnectionSupplier pConnectionSupplier, boolean pCloseAfterStatement,
                           IValueSerializer pSerializer, String pTableName, String pIdColumnName)
  {
    super(pPlatform, pConnectionSupplier, pCloseAfterStatement, pSerializer, pIdColumnName);
    tableName = StringUtility.requireNotEmpty(pTableName, "table name");
  }

  @Override
  public void dropTable()
  {
    boolean result = super.dropTable(tableName);
    if (!result)
      throw new IllegalStateException("The table " + tableName + " is not existing anymore!");
  }

  @Override
  public void addColumn(IColumnDefinition pColumnDefinition)
  {
    super.addColumn(tableName, pColumnDefinition);
  }

  @Override
  public void removeColumn(IColumnIdentification<?> pColumn)
  {
    super.removeColumn(tableName, pColumn);
  }

  @Override
  public boolean hasTable()
  {
    return super.hasTable(tableName);
  }

  @Override
  public void ifTableNotExistingCreate(Consumer<Create> pCreateStatement)
  {
    super.ifTableNotExistingCreate(tableName, pCreateStatement);
  }

  @Override
  public int getColumnCount()
  {
    return super.getColumnCount(tableName);
  }

  @Override
  public boolean hasColumn(String pColumnName)
  {
    return super.hasColumn(tableName, pColumnName);
  }

  @Override
  public Set<String> getAllColumnNames()
  {
    return super.getAllColumnNames(tableName);
  }

  @Override
  protected <RESULT, STATEMENT extends AbstractSQLStatement<RESULT, STATEMENT>> STATEMENT configureStatementBeforeExecution(
      STATEMENT pStatement)
  {
    pStatement.setTableName(tableName);
    return pStatement;
  }
}
