package de.adito.ojcms.sqlbuilder.statements.types.select;

import de.adito.ojcms.sqlbuilder.AbstractSQLBuilder;
import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.executors.IStatementExecutor;
import de.adito.ojcms.sqlbuilder.format.*;
import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;
import de.adito.ojcms.sqlbuilder.result.Result;
import de.adito.ojcms.sqlbuilder.serialization.IValueSerializer;
import de.adito.ojcms.sqlbuilder.statements.AbstractConditionStatement;
import de.adito.ojcms.sqlbuilder.util.*;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;

import static de.adito.ojcms.sqlbuilder.format.EFormatConstant.*;
import static de.adito.ojcms.sqlbuilder.format.ESeparator.COMMA_WITH_WHITESPACE;

/**
 * An abstract base class for select statements.
 * The statement will return a {@link Result}.
 *
 * @param <SELECT> the runtime type of the final select statement
 * @author Simon Danner, 26.04.2018
 */
public abstract class AbstractSelect<SELECT extends AbstractSelect<SELECT>>
    extends AbstractConditionStatement<SelectModifiers, Result, ResultSet, SELECT>
{
  private final List<IColumnIdentification<?>> columnsToSelect = new ArrayList<>();

  /**
   * Creates a new select statement.
   *
   * @param pStatementExecutor the executor for the statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pPlatform          the database platform used for this select statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column
   */
  protected AbstractSelect(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, IDatabasePlatform pPlatform,
                           IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pPlatform, pSerializer, new SelectModifiers(), pIdColumnName);
  }

  /**
   * Determines, on which database table this statement should be executed.
   *
   * @param pTableName the name of the database table
   * @return the statement itself to enable a pipelining mechanism
   */
  public SELECT from(String pTableName)
  {
    return setTableName(pTableName);
  }

  /**
   * Adds the id column as column to select.
   *
   * @return the statement itself to enable a pipelining mechanism
   */
  public SELECT withId()
  {
    columnsToSelect.add(idColumnIdentification);
    //noinspection unchecked
    return (SELECT) this;
  }

  /**
   * Terminates the statement and returns the amount of rows selected by the statement.
   *
   * @return the number of rows selected
   */
  public int countRows()
  {
    if (columnsToSelect.isEmpty())
      withId();

    modifiers.setCount(true);

    try
    {
      final ResultSet resultSet = _query();
      return resultSet.next() ? resultSet.getInt(StaticConstants.COUNT_AS) : 0;
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE);
    }
  }

  /**
   * Determines, if the select statement has a result, which means the number of rows is greater than 0.
   *
   * @return <tt>true</tt>, if the select statement has a result
   */
  public boolean hasResult()
  {
    return countRows() > 0;
  }

  /**
   * Configures the select statement to provide distinct values only.
   *
   * @return the select statement itself to enable a pipelining mechanism
   */
  public SELECT distinct()
  {
    modifiers.setDistinct(true);
    //noinspection unchecked
    return (SELECT) this;
  }

  /**
   * Adds columns to select through the statement. Must be at least one column.
   *
   * @param pColumns the column identifications the select
   * @return the select statement itself to enable a pipelining mechanism
   */
  protected SELECT addColumns(Collection<IColumnIdentification<?>> pColumns)
  {
    if (pColumns.isEmpty())
      throw new OJDatabaseException("At least one column has to be selected!");

    columnsToSelect.addAll(pColumns);
    //noinspection unchecked
    return (SELECT) this;
  }

  @Override
  protected Result doQuery()
  {
    return new Result(columnsToSelect, idColumnIdentification, serializer, _query());
  }

  /**
   * Executes the statement in the database.
   *
   * @return the result set of the statement
   */
  private ResultSet _query()
  {
    final Supplier<String> columnSupplier = () -> StatementFormatter.join(columnsToSelect.stream() //
        .map(IColumnIdentification::getColumnName), COMMA_WITH_WHITESPACE);

    return executeStatement(EFormatter.SELECT.create(databasePlatform, idColumnIdentification.getColumnName()) //
        .conditional(modifiers.distinct(), pFormat -> pFormat.appendConstant(DISTINCT)) //
        .conditionalOrElse(modifiers.count(), //
            //with count
            pFormat -> pFormat.appendConstant(COUNT, columnSupplier.get()), //
            //without count
            pFormat -> pFormat.appendMultiple(columnsToSelect.stream(), COMMA_WITH_WHITESPACE)) //
        .appendTableName(getTableName()) //
        .appendWhereCondition(modifiers));
  }
}
