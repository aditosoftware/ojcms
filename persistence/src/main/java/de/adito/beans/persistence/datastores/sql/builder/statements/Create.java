package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;

import java.util.List;
import java.util.stream.*;

/**
 * A create statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Create extends AbstractBaseStatement<Void, Create>
{
  private final IColumnDefinition<Integer> idColumnDefinition;
  private boolean withIdColumn = false;
  private IColumnDefinition[] columns;

  /**
   * Creates the create statement.
   *
   * @param pStatementExecutor the executor fot this statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column for this table
   */
  public Create(IStatementExecutor<Void> pStatementExecutor, EDatabaseType pDatabaseType, IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pDatabaseType, pSerializer);
    idColumnDefinition = IColumnDefinition.of(pIdColumnName, EColumnType.INT, Integer.class, EColumnModifier.PRIMARY_KEY, EColumnModifier.NOT_NULL);
  }

  /**
   * Sets the table name for this create statement.
   *
   * @param pTableName the table name
   * @return the create statement itself to enable a pipelining mechanism
   */
  public Create tableName(String pTableName)
  {
    return super.setTableName(pTableName);
  }

  /**
   * Determines the columns to create.
   *
   * @param pColumnDefinitions the column definitions to create
   * @return the create statement itself to enable a pipelining mechanism
   */
  public Create columns(IColumnDefinition... pColumnDefinitions)
  {
    columns = pColumnDefinitions;
    return this;
  }

  /**
   * Configures the create statement to include a id column additionally.
   *
   * @return the create statement itself to enable a pipelining mechanism
   */
  public Create withIdColumn()
  {
    withIdColumn = true;
    return this;
  }

  /**
   * Executes the statement and creates the table in the database.
   */
  public void create()
  {
    String query = "CREATE TABLE " + getTableName() + " ("
        + (withIdColumn ? Stream.concat(Stream.of(idColumnDefinition), Stream.of(columns)) : Stream.of(columns))
        .map(pColumnDefinition -> pColumnDefinition.toStatementFormat(databaseType))
        .collect(Collectors.joining(",\n")) + _primaryKey() + ")";
    executeStatement(query);
  }

  /**
   * Creates the statement string for the primary key columns.
   * Will be empty, if there is no primary key.
   *
   * @return the primary key part for the create statement
   */
  private String _primaryKey()
  {
    List<IColumnDefinition> primaryKeyColumns = Stream.concat(withIdColumn ? Stream.of(idColumnDefinition) : Stream.empty(),
                                                              Stream.of(columns).filter(IColumnDefinition::isPrimaryKey))
        .collect(Collectors.toList());
    return primaryKeyColumns.isEmpty() ? "" : ",\nPRIMARY KEY(" + primaryKeyColumns.stream()
        .map(pColumnDefinition -> pColumnDefinition.getColumnName().toUpperCase())
        .collect(Collectors.joining(", ")) + ")";
  }
}
