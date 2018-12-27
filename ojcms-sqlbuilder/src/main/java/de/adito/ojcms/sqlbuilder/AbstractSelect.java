package de.adito.ojcms.sqlbuilder;

import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.format.*;
import de.adito.ojcms.sqlbuilder.result.Result;
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
public abstract class AbstractSelect<SELECT extends AbstractSelect<SELECT>> extends AbstractSQLStatement<SelectModifiers, Result, ResultSet, SELECT>
{
  private List<IColumnIdentification<?>> columnsToSelect = new ArrayList<>();
  private boolean shouldSelectAllColumns = true;

  /**
   * Creates a new select statement.
   *
   * @param pStatementExecutor the executor for the statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this select statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column
   */
  protected AbstractSelect(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                           IValueSerializer pSerializer, String pIdColumnName)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, new SelectModifiers(), pIdColumnName);
  }

  /**
   * Terminates the statement and returns the amount of rows selected by the statement.
   *
   * @return the number of rows selected
   */
  public int countRows()
  {
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
   * Terminates the statement and returns the amount columns selected by the statement.
   *
   * @return the number of columns selected
   */
  public int countColumns()
  {
    try
    {
      return shouldSelectAllColumns ? _query().getMetaData().getColumnCount() : columnsToSelect.size();
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
   * The id column identification for this statement.
   *
   * @return an id column identification
   */
  public IColumnIdentification<Integer> idColumn()
  {
    return idColumnIdentification;
  }

  /**
   * Adds columns to select through the statement. Must be at least one column.
   *
   * @param pColumns the column identifications the select
   * @return the select statement itself to enable a pipelining mechanism
   */
  protected SELECT addColumns(IColumnIdentification<?>... pColumns)
  {
    if (pColumns.length == 0)
      throw new OJDatabaseException("At least one column has to be selected!");
    columnsToSelect.addAll(Arrays.asList(pColumns));
    shouldSelectAllColumns = false;
    //noinspection unchecked
    return (SELECT) this;
  }

  /**
   * Configures the select statement to select all columns of the database table.
   * This is the default, but it may be used for a better readability.
   *
   * @return the select statement itself to enable a pipelining mechanism
   */
  protected SELECT selectAllColumns()
  {
    columnsToSelect.clear();
    shouldSelectAllColumns = true;
    //noinspection unchecked
    return (SELECT) this;
  }

  @Override
  protected Result doQuery()
  {
    return new Result(serializer, _query());
  }

  /**
   * Executes the statement in the database.
   *
   * @return the result set of the statement
   */
  private ResultSet _query()
  {
    final Supplier<String> columnSupplier = () -> StatementFormatter.join(columnsToSelect.stream().map(IColumnIdentification::getColumnName),
                                                                          COMMA_WITH_WHITESPACE);
    final StatementFormatter statement = EFormatter.SELECT.create(databaseType, idColumnIdentification.getColumnName())
        .conditional(modifiers.distinct(), pFormat -> pFormat.appendConstant(DISTINCT))
        .conditionalOrElse(modifiers.count(),
                           //with count
                           pFormat -> pFormat.conditionalOrElse(shouldSelectAllColumns,
                                                                //use * for all columns
                                                                pInner -> pInner.appendConstant(COUNT, STAR.toStatementFormat()),
                                                                //the columns to select are defined
                                                                pInner -> pInner.appendConstant(COUNT, columnSupplier.get())),
                           //without count
                           pFormat -> pFormat.conditionalOrElse(shouldSelectAllColumns,
                                                                //use * for all columns
                                                                pInner -> pInner.appendConstant(STAR),
                                                                //the columns to select are defined
                                                                pInner -> pInner.appendMultiple(columnsToSelect.stream(), COMMA_WITH_WHITESPACE)))
        .appendTableName(getTableName())
        .appendWhereCondition(modifiers);
    return executeStatement(statement);
  }
}
