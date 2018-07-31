package de.adito.beans.persistence.datastores.sql.builder.statements;

import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.column.*;
import de.adito.beans.persistence.datastores.sql.builder.format.*;

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
   * @param pStatementExecutor the executor for this statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column for this table
   */
  public Create(IStatementExecutor<Void> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer);
    idColumnDefinition = IColumnDefinition.of(pIdColumnName, EColumnType.INT.create().primaryKey().modifiers(EColumnModifier.NOT_NULL));
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
    final StatementFormatter statement = EFormatter.CREATE.create(databaseType, idColumnDefinition.getColumnName())
        .appendTableName(getTableName())
        .openBracket()
        .appendMultiple(columns.stream(), ESeparator.COMMA, ESeparator.NEW_LINE)
        .appendFunctional(this::_primaryKeys)
        .appendFunctional(this::_foreignKeys)
        .closeBracket();
    executeStatement(statement);
  }

  /**
   * Adds a primary key to the statement format, if there are columns markes as primary keys.
   *
   * @param pFormatter the formatter for the statement
   */
  private void _primaryKeys(StatementFormatter pFormatter)
  {
    List<String> primaryKeyColumnNames = columns.stream()
        .filter(pColumn -> pColumn.getColumnType().isPrimaryKey())
        .map(pColumnDefinition -> pColumnDefinition.getColumnName().toUpperCase())
        .collect(Collectors.toList());
    if (primaryKeyColumnNames.isEmpty())
      return;
    pFormatter.appendSeparator(ESeparator.COMMA, ESeparator.NEW_LINE);
    pFormatter.appendConstant(EFormatConstant.PRIMARY_KEY, String.join(", ", primaryKeyColumnNames));
  }

  /**
   * Adds foreign key constraints to the statement format, if present.
   * The referenced database tables of the foreign keys may be created, if necessary.
   *
   * @param pFormatter the formatter for the statement
   */
  private void _foreignKeys(StatementFormatter pFormatter)
  {
    Map<String, IForeignKey> foreignKeyMapping = columns.stream()
        .filter(pColumn -> pColumn.getColumnType().getForeignKey() != null)
        .collect(Collectors.toMap(IColumnDefinition::getColumnName, pColumn -> pColumn.getColumnType().getForeignKey()));
    if (foreignKeyMapping.isEmpty())
      return;
    final OJSQLBuilder tableChecker = OJSQLBuilderFactory.newSQLBuilder(builder)
        .create();
    foreignKeyMapping.forEach((pColumn, pReference) -> {
      if (!tableChecker.hasTable(pReference.getTableName()))
        pReference.createReferencedTable(tableChecker.getConnectionInfo()); //Create referenced table, if necessary
      pFormatter.appendSeparator(ESeparator.COMMA, ESeparator.NEW_LINE);
      pFormatter.appendConstant(EFormatConstant.FOREIGN_KEY, pColumn, pReference.getTableName(),
                                String.join(", ", pReference.getColumnNames()));
    });
  }
}
