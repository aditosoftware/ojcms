package de.adito.beans.persistence.datastores.sql.builder;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.format.*;
import de.adito.beans.persistence.datastores.sql.builder.result.Result;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

import java.sql.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * An abstract base class for select statements.
 * The statement will return a {@link Result}.
 *
 * @param <SELECT> the concrete type of the final select statement
 * @author Simon Danner, 26.04.2018
 */
public abstract class AbstractSelect<SELECT extends AbstractSelect<SELECT>> extends AbstractSQLStatement<SelectModifiers, Result, ResultSet, SELECT>
{
  private final List<IColumnIdentification<?>> columns;
  private final String idColumnName;

  /**
   * Creates a new select statement.
   *
   * @param pStatementExecutor the executor for the statement
   * @param pBuilder           the builder that created this statement to use other kinds of statements for a concrete statement
   * @param pDatabaseType      the database type used for this select statement
   * @param pSerializer        the value serializer
   * @param pIdColumnName      the name of the id column
   * @param pColumns           the column names to select
   */
  protected AbstractSelect(IStatementExecutor<ResultSet> pStatementExecutor, AbstractSQLBuilder pBuilder, EDatabaseType pDatabaseType,
                           IValueSerializer pSerializer, String pIdColumnName, IColumnIdentification<?>... pColumns)
  {
    super(pStatementExecutor, pBuilder, pDatabaseType, pSerializer, new SelectModifiers());
    columns = Arrays.asList(pColumns);
    idColumnName = pIdColumnName;
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
      ResultSet resultSet = _query();
      return resultSet.next() ? resultSet.getInt(EFormatConstant.StaticConstants.COUNT_AS) : 0;
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
      return _query().getMetaData().getColumnCount();
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

  @Override
  protected Result doQuery()
  {
    return new Result(serializer, _query(), idColumnName);
  }

  /**
   * Executes the statement in the database.
   *
   * @return the result set of the statement
   */
  private ResultSet _query()
  {
    final Supplier<String> columnSupplier = () -> StatementFormatter.join(columns.stream().map(IColumnIdentification::getColumnName),
                                                                          ESeparator.COMMA_WITH_WHITESPACE);
    final StatementFormatter statement = EFormatter.SELECT.create(databaseType, idColumnName)
        .conditional(modifiers.distinct(), pFormatter -> pFormatter.appendConstant(EFormatConstant.DISTINCT))
        .conditionalOrElse(modifiers.count(),
                           //with count
                           pFormatter -> pFormatter.conditionalOrElse(columns.isEmpty(),
                                                                      //use * for all columns
                                                                      pInner -> pInner.appendConstant(EFormatConstant.COUNT, EFormatConstant.STAR.toStatementFormat()),
                                                                      //the columns to select are defined
                                                                      pInner -> pInner.appendConstant(EFormatConstant.COUNT, columnSupplier.get())),
                           //without count
                           pFormatter -> pFormatter.conditionalOrElse(columns.isEmpty(),
                                                                      //use * for all columns
                                                                      pInner -> pInner.appendConstant(EFormatConstant.STAR),
                                                                      //the columns to select are defined
                                                                      pInner -> pInner.appendMultiple(columns.stream(), ESeparator.COMMA_WITH_WHITESPACE)))
        .appendTableName(getTableName())
        .appendWhereCondition(modifiers);
    return executeStatement(statement);
  }
}
