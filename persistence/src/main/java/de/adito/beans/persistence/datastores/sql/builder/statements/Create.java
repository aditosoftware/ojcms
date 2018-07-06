package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.column.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A create statement.
 *
 * @author Simon Danner, 26.04.2018
 */
public class Create extends AbstractBaseStatement<Void, Create>
{
  private final IColumnDefinition idColumnDefinition;
  private final List<IColumnDefinition> columns = new ArrayList<>();

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
    idColumnDefinition = IColumnDefinition.of(pIdColumnName, EColumnType.INT.primaryKey().modifiers(EColumnModifier.NOT_NULL));
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
    columns.addAll(Arrays.asList(pColumnDefinitions));
    return this;
  }

  /**
   * Configures the create statement to include an id column additionally.
   *
   * @return the create statement itself to enable a pipelining mechanism
   */
  public Create withIdColumn()
  {
    columns.add(0, idColumnDefinition);
    return this;
  }

  /**
   * Executes the statement and creates the table in the database.
   */
  public void create()
  {
    String query = "CREATE TABLE " + getTableName() + " (" + columns.stream()
        .map(pColumnDefinition -> pColumnDefinition.toStatementFormat(databaseType))
        .collect(Collectors.joining(",\n")) +
        _primaryKeys() +
        _foreignKeys() + ")";
    executeStatement(query);
  }

  /**
   * Creates the statement string for the primary key columns.
   * Will be empty, if there is no primary key.
   *
   * @return the primary key part of the create statement
   */
  private String _primaryKeys()
  {
    List<String> primaryKeyColumnNames = columns.stream()
        .filter(pColumn -> pColumn.getColumnType().isPrimaryKey())
        .map(pColumnDefinition -> pColumnDefinition.getColumnName().toUpperCase())
        .collect(Collectors.toList());
    return primaryKeyColumnNames.isEmpty() ? "" : ",\nPRIMARY KEY(" + primaryKeyColumnNames.stream()
        .collect(Collectors.joining(", ")) + ")";
  }

  /**
   * Creates the statement string for the foreign keys.
   * Will be empty, if there are no foreign key constraints.
   *
   * @return the foreign key part of the create statement
   */
  private String _foreignKeys()
  {
    Map<String, IForeignKey> foreignKeyMapping = columns.stream()
        .filter(pColumn -> pColumn.getColumnType().getForeignKey() != null)
        .collect(Collectors.toMap(IColumnDefinition::getColumnName, pColumn -> pColumn.getColumnType().getForeignKey()));
    return foreignKeyMapping.isEmpty() ? "" : "\n" + foreignKeyMapping.entrySet().stream()
        .map(pEntry -> "FOREIGN KEY (" + pEntry.getKey() + ") REFERENCES " + pEntry.getValue().toStatementFormat(databaseType))
        .collect(Collectors.joining(",\n"));
  }
}
