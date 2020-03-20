package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.platform.connection.IDatabaseConnectionSupplier;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.types.Create;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Implementation of the SQL builder.
 *
 * @author Simon Danner, 15.05.2018
 */
final class OJSQLBuilderImpl extends AbstractSQLBuilder implements OJSQLBuilder
{
  /**
   * Creates a new builder.
   *
   * @param pPlatform            the database platform to use for this builder
   * @param pConnectionSupplier  the database platform based connection supplier
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pSerializer          the value serializer
   * @param pIdColumnName        a global id column name for this builder instance
   */
  OJSQLBuilderImpl(IDatabasePlatform pPlatform, IDatabaseConnectionSupplier pConnectionSupplier, boolean pCloseAfterStatement,
                   IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pPlatform, pConnectionSupplier, pCloseAfterStatement, pSerializer, pIdColumnName);
  }

  @Override
  public boolean dropTable(String pTableName)
  {
    return super.dropTable(pTableName);
  }

  @Override
  public void addColumn(String pTableName, IColumnDefinition pColumnDefinition)
  {
    super.addColumn(pTableName, pColumnDefinition);
  }

  @Override
  public void removeColumn(String pTableName, IColumnIdentification<?> pColumn)
  {
    super.removeColumn(pTableName, pColumn);
  }

  @Override
  public boolean hasTable(String pTableName)
  {
    return super.hasTable(pTableName);
  }

  @Override
  public void ifTableNotExistingCreate(String pTableName, Consumer<Create> pCreateStatement)
  {
    super.ifTableNotExistingCreate(pTableName, pCreateStatement);
  }

  @Override
  public Set<String> getAllTableNames()
  {
    return super.getAllTableNames();
  }

  @Override
  public int getColumnCount(String pTableName)
  {
    return super.getColumnCount(pTableName);
  }

  @Override
  public boolean hasColumn(String pTableName, String pColumnName)
  {
    return super.hasColumn(pTableName, pColumnName);
  }

  @Override
  public Set<String> getAllColumnNames(String pTableName)
  {
    return super.getAllColumnNames(pTableName);
  }
}
