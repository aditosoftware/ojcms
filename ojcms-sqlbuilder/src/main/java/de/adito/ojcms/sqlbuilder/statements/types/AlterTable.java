package de.adito.ojcms.sqlbuilder.statements.types;

import de.adito.ojcms.sqlbuilder.*;
import de.adito.ojcms.sqlbuilder.definition.column.IColumnDefinition;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.format.*;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;

import java.util.*;

/**
 * An alter table statement.
 *
 * @author Simon Danner, 18.03.2020
 */
public class AlterTable extends AbstractSQLStatement<Void, AlterTable>
{
  private final Set<IColumnDefinition> columnsToAdd = new HashSet<>();
  private final Set<String> columnsToDrop = new HashSet<>();

  /**
   * Creates the alter table statement.
   *
   * @param pExecutor     the executor for the statements
   * @param pBuilder      the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform     the database platform used for this statement
   * @param pSerializer   the value serializer
   * @param pIdColumnName the name of the global id column
   */
  public AlterTable(IStatementExecutor<Void> pExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                    IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pExecutor, pBuilder, pPlatform, pSerializer, pIdColumnName);
  }

  /**
   * Defines the table to alter.
   *
   * @param pTableName the name of the table to alter
   * @return the alter table statement itself to enable a pipelining mechanism
   */
  public AlterTable table(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Defines the columns to add to the table.
   *
   * @param pColumnsToAdd multiple columns to add
   * @return the alter table statement itself to enable a pipelining mechanism
   */
  public AlterTable columnsToAdd(IColumnDefinition... pColumnsToAdd)
  {
    return columnsToAdd(Arrays.asList(pColumnsToAdd));
  }

  /**
   * Defines the columns to add to the table.
   *
   * @param pColumnsToAdd multiple columns to add
   * @return the alter table statement itself to enable a pipelining mechanism
   */
  public AlterTable columnsToAdd(Collection<IColumnDefinition> pColumnsToAdd)
  {
    columnsToAdd.addAll(pColumnsToAdd);
    return this;
  }

  /**
   * Defines the columns to drop from the table.
   *
   * @param pColumnsToDrop multiple column names to drop
   * @return the alter table statement itself to enable a pipelining mechanism
   */
  public AlterTable columnsToDrop(String... pColumnsToDrop)
  {
    return columnsToDrop(Arrays.asList(pColumnsToDrop));
  }

  /**
   * Defines the columns to drop from the table.
   *
   * @param pColumnsToDrop multiple column names to drop
   * @return the alter table statement itself to enable a pipelining mechanism
   */
  public AlterTable columnsToDrop(Collection<String> pColumnsToDrop)
  {
    columnsToDrop.addAll(pColumnsToDrop);
    return this;
  }

  /**
   * Executes the statement and alters the table in the database.
   */
  public void alter()
  {
    if (columnsToAdd.isEmpty() && columnsToDrop.isEmpty())
      throw new OJDatabaseException("At least one column must be defined to be altered!");

    for (IColumnDefinition columnToAdd : columnsToAdd)
      executeStatement(EFormatter.ALTER.create(databasePlatform, idColumnIdentification.getColumnName()) //
          .appendTableName(getTableName()) //
          .appendConstant(EFormatConstant.ADD) //
          .appendStatement(columnToAdd));

    for (String columnNameToDrop : columnsToDrop)
      executeStatement(EFormatter.ALTER.create(databasePlatform, idColumnIdentification.getColumnName()) //
          .appendTableName(getTableName()) //
          .appendConstant(EFormatConstant.DROP_COLUMN, columnNameToDrop));
  }
}
