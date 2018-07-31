package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.column.IColumnDefinition;
import de.adito.beans.persistence.datastores.sql.builder.statements.Create;
import de.adito.beans.persistence.datastores.sql.builder.util.DBConnectionInfo;

import java.util.List;
import java.util.function.Consumer;

/**
 * A SQL statement builder.
 * The database statements can be used and adapted in a functional way like the Java streams.
 * This builder can be used for multiple database tables.
 * This builder can be configured via {@link OJSQLBuilderFactory}.
 *
 * @author Simon Danner, 15.05.2018
 */
public class OJSQLBuilder extends AbstractSQLBuilder
{
  /**
   * Creates a new builder.
   *
   * @param pDatabaseType        the database type to use for this builder
   * @param pConnectionInfo      the database connection information
   * @param pCloseAfterStatement <tt>true</tt>, if the connection should be closed after executing one statement
   * @param pSerializer          the value serializer
   * @param pIdColumnName        a global id column name for this builder instance
   */
  OJSQLBuilder(EDatabaseType pDatabaseType, DBConnectionInfo pConnectionInfo, boolean pCloseAfterStatement,
               IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pDatabaseType, pConnectionInfo, pCloseAfterStatement, pSerializer, pIdColumnName);
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
  public List<String> getAllTableNames()
  {
    return super.getAllTableNames();
  }

  @Override
  public int getColumnCount(String pTableName)
  {
    return super.getColumnCount(pTableName);
  }
}
