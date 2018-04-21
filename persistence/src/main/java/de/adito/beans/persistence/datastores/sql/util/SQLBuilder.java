package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IField;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.persistence.datastores.sql.IDatabaseConstants;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * A SQL builder optimized for the bean persistence framework.
 * The database statements can be used and adapted in an functional way like the Java streams.
 * Important note: A instance of this builder is based on a database connection. The builder does not close the connection.
 * The builder may be used for multiple statements. The connection has to be closed outside of this class.
 *
 * @author Simon Danner, 19.02.2018
 */
public class SQLBuilder
{
  private final Connection connection;

  /**
   * Creates a new builder.
   * It is based on a database connection, which will not be close from this builder (may be used for multiple queries)
   *
   * @param pConnection the database connection
   */
  public SQLBuilder(Connection pConnection)
  {
    connection = pConnection;
  }

  /**
   * Creates a new create statement.
   *
   * @param pTableName the name of the table, that should be created
   * @return a create statement
   */
  public Create create(String pTableName)
  {
    return new Create(pTableName);
  }

  /**
   * Creates a new insert statement.
   *
   * @return a insert statement
   */
  public Insert insert()
  {
    return new Insert();
  }

  /**
   * Creates a new update statement.
   *
   * @return a update statement
   */
  public Update update()
  {
    return new Update();
  }

  /**
   * Creates a new delete statement.
   *
   * @return a delete statement
   */
  public Delete delete()
  {
    return new Delete();
  }

  /**
   * Creates a new select statement.
   *
   * @param pFields the variable collection of fields/columns that should be selected (none for select-all)
   * @return the select statement
   */
  public Select select(IField<?>... pFields)
  {
    return pFields == null || pFields.length == 0 ? new Select() : new Select(pFields);
  }

  /**
   * Create a new single select statement.
   * This query will select one certain column only.
   *
   * @param pField the bean field, which defines the column to select
   * @param <TYPE> the data type of the bean field (may be the result of the query)
   * @return the single select statement
   */
  public <TYPE> SingleSelect<TYPE> selectOne(IField<TYPE> pField)
  {
    return new SingleSelect<>(pField);
  }

  /**
   * Performs a query with no result on the given database connection.
   *
   * @param pQuery the query to execute
   */
  private void _executeNoResultQuery(String pQuery)
  {
    try
    {
      connection.createStatement().execute(pQuery);
    }
    catch (SQLException pE)
    {
      throw new BeanSQLException(pQuery, pE);
    }
  }

  /**
   * Marker interface for all statements created by this builder.
   */
  public interface IStatement
  {
  }

  /**
   * Defines a create statement.
   */
  public class Create implements IStatement
  {
    private final String tableName;
    private boolean withIdColumn = false;
    private List<IField<?>> columns;

    /**
     * Creates a new create statement.
     *
     * @param pTableName the name of the table to create
     */
    public Create(String pTableName)
    {
      tableName = pTableName;
    }

    /**
     * Determines the columns to create.
     *
     * @param pColumns the columns based on a list of bean fields
     * @return the create statement itself to enable a pipelining mechanism
     */
    public Create columns(List<IField<?>> pColumns)
    {
      columns = new ArrayList<>(pColumns);
      return this;
    }

    /**
     * Configures the create statement to include a id column as well.
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
     * For now, the columns are all 'varchar(255)'.
     */
    public void create()
    {
      String query = "CREATE TABLE " + tableName + " ("
          + (withIdColumn ? IDatabaseConstants.ID_COLUMN + " INTEGER NOT NULL,\n" : "")
          + columns.stream()
          .map(pColumn -> pColumn.getName() + " varchar(255)")
          .collect(Collectors.joining(",\n")) + ")";
      try
      {
        connection.createStatement().execute(query);
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(query, pE);
      }
    }
  }

  /**
   * Defines an insert statement.
   */
  public class Insert implements IStatement
  {
    private String tableName;
    private FieldTuple[] values;
    private int index = -1;

    /**
     * Determines the table to perform the insertion
     *
     * @param pTableName the table name
     * @return the insert statement itself to enable a pipelining mechanism
     */
    public Insert into(String pTableName)
    {
      tableName = pTableName;
      return this;
    }

    /**
     * Determines the columns and associated values as field tuples.
     *
     * @param pFieldTuples the field tuples to insert
     * @return the insert statement itself to enable a pipelining mechanism
     */
    public Insert values(FieldTuple<?>... pFieldTuples)
    {
      if (pFieldTuples.length == 0)
        throw new BeanSQLException("The tupels to insert can not be empty!");

      values = pFieldTuples;
      return this;
    }

    /**
     * Determines an index to insert the new row.
     *
     * @param pIndex the index to insert
     * @return the insert statement itself to enable a pipelining mechanism
     */
    public Insert atIndex(int pIndex)
    {
      if (pIndex < 0)
        throw new IllegalArgumentException("The index can not be smaller than 0! index: " + pIndex);

      index = pIndex;
      return this;
    }

    /**
     * Executes the insertion.
     */
    public void insert()
    {
      final String id = IDatabaseConstants.ID_COLUMN;
      if (index >= 0)
        //Increment all ids after the index to insert
        _executeNoResultQuery("UPDATE " + tableName + " SET " + id + " = " + id + "+1 WHERE " + id + ">=" + index);

      _executeNoResultQuery("INSERT INTO " + tableName + " (" + (index >= 0 ? id + ", " : "") + _enumerate(values, pTuple -> pTuple.getField().getName())
                                + ") VALUES (" + (index >= 0 ? index + ", " : "") + _enumerate(values, SQLSerializer::toPersistent) + ")");
    }

    /**
     * Enumerates information of the tuples as string separated by commas.
     *
     * @param pTuples   the field tuples (field + value)
     * @param pResolver a resolver function to retrieve a value based on a tuple
     * @return the concatenated string
     */
    private String _enumerate(FieldTuple<?>[] pTuples, Function<FieldTuple<?>, String> pResolver)
    {
      return Stream.of(pTuples)
          .map(pResolver)
          .collect(Collectors.joining(", "));
    }
  }

  /**
   * Defines an update statement.
   */
  public class Update extends _AbstractStatement<_Modifiers, Void, Update>
  {
    private FieldTuple<?>[] changes;

    /**
     * Creates a new update statement.
     */
    public Update()
    {
      super(new _Modifiers());
    }

    /**
     * Determines the field tuples (field + value) to update in the given table.
     *
     * @param pChanges the changes defined as field tuples
     * @return the update statement itself to enable a pipelining mechanism
     */
    public Update set(FieldTuple<?>... pChanges)
    {
      changes = pChanges;
      return this;
    }

    /**
     * Performs the update in the database.
     */
    public void update()
    {
      doQuery();
    }

    @Override
    protected Void doQuery()
    {
      if (changes.length > 0)
        _executeNoResultQuery("UPDATE " + tableName + " SET " + _changes() + modifiers.where());
      return null;
    }

    /**
     * Enumerates the changes as one string in this format: 'FIELDNAME = VALUE'.
     *
     * @return the concatenated string
     */
    private String _changes()
    {
      return Stream.of(changes)
          .map(pChange -> pChange.getField().getName() + " = '" + SQLSerializer.toPersistent(pChange) + "'")
          .collect(Collectors.joining(", "));
    }
  }

  /**
   * Defines a delete statement.
   */
  public class Delete extends _AbstractStatement<_Modifiers, Boolean, Delete>
  {
    /**
     * Create a new delete statement.
     */
    public Delete()
    {
      super(new _Modifiers());
    }

    /**
     * Performs the deletion.
     *
     * @return <tt>true</tt>, if the deletion was successful
     */
    public boolean delete()
    {
      return doQuery();
    }

    @Override
    protected Boolean doQuery()
    {
      String query = "DELETE FROM " + tableName + modifiers.where();
      try
      {
        connection.createStatement().execute(query);
        return true;
      }
      catch (SQLException pE)
      {
        return false;
      }
    }
  }

  /**
   * Defines a select statement/query.
   */
  public class Select extends _AbstractSelect<Select>
  {
    public Select(IField<?>... pFields)
    {
      super(pFields);
    }

    /**
     * The first result row, if present.
     *
     * @return an optional result row
     */
    public Optional<ResultRow> firstResult()
    {
      return fullResult().getFirst();
    }

    /**
     * The full result of the query (0-n rows).
     *
     * @return the result of the query
     */
    public Result fullResult()
    {
      return doQuery();
    }
  }

  /**
   * Defines a single column select statement.
   *
   * @param <TYPE> the type of the result (data value of the bean field defining the column)
   */
  public class SingleSelect<TYPE> extends _AbstractSelect<SingleSelect<TYPE>>
  {
    private final IField<TYPE> field;

    /**
     * Create a new single select based on a bean field.
     *
     * @param pField the bean field defining the column to select
     */
    public SingleSelect(IField<TYPE> pField)
    {
      super(pField);
      field = pField;
    }

    /**
     * The result of the first row.
     *
     * @return the result of the query (first row)
     * @throws NoResultException if, the result is empty
     */
    public TYPE firstResult() throws NoResultException
    {
      return fullResult().getFirst();
    }

    /**
     * The full result of the query.
     * In this case this is a set of values of the field's data type for each result row.
     *
     * @return the result of the query
     */
    public SingleColumnResult<TYPE> fullResult()
    {
      return new SingleColumnResult<>(field, doQuery());
    }
  }

  /**
   * Defines the result of a select statement.
   */
  public class Result implements Iterable<ResultRow>
  {
    private final ResultSet resultSet;
    private final ResultRow row;

    /**
     * Creates a new result.
     *
     * @param pResult the result set from the query
     */
    public Result(ResultSet pResult)
    {
      resultSet = pResult;
      row = new ResultRow(resultSet);
    }

    /**
     * The first row of the whole result.
     *
     * @return an optional result row
     */
    public Optional<ResultRow> getFirst()
    {
      try
      {
        return resultSet.next() ? Optional.of(row) : Optional.empty();
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(pE);
      }
    }

    @NotNull
    @Override
    public Iterator<ResultRow> iterator()
    {
      return new Iterator<ResultRow>()
      {
        @Override
        public boolean hasNext()
        {
          try
          {
            return !resultSet.isAfterLast();
          }
          catch (SQLException pE)
          {
            throw new BeanSQLException(pE);
          }
        }

        @Override
        public ResultRow next()
        {
          if (hasNext())
          {
            try
            {
              //Just move the cursor of the result set - the result row stays the same
              resultSet.next();
              return row;
            }
            catch (SQLException pE)
            {
              throw new BeanSQLException(pE);
            }
          }
          throw new NoSuchElementException();
        }
      };
    }
  }

  /**
   * Defines a single row of the result of a select statement.
   * A instance may be used for multiple rows, because it is based on a {@link ResultSet}, where the cursor can be moved to the next row.
   */
  public class ResultRow
  {
    private final ResultSet resultSet;

    /**
     * Create a new result row.
     *
     * @param pResultSet the sql result set, which it is based on
     */
    public ResultRow(ResultSet pResultSet)
    {
      resultSet = pResultSet;
    }

    /**
     * Determines, if the result row contains a certain bean field (column).
     *
     * @param pField the bean field
     * @return <tt>true</tt>, if the field/column is contained
     */
    public boolean hasColumn(IField<?> pField)
    {
      return _hasColumn(pField.getName());
    }

    /**
     * The value of this row for a certain field/column.
     *
     * @param pField the bean field/column
     * @param <TYPE> the field's data type
     * @return the value
     */
    public <TYPE> TYPE get(IField<TYPE> pField)
    {
      try
      {
        if (!hasColumn(pField))
          throw new RuntimeException("The bean field '" + pField.getName() + "' is not contained in the result!");

        String serialValue = resultSet.getString(pField.getName());
        return SQLSerializer.fromPersistent(pField, serialValue);
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(pE);
      }
    }

    /**
     * The id/index of the row, if available.
     *
     * @return the id of this result row (throws a runtime exception, if not available)
     */
    public Integer getIdIfAvailable()
    {
      try
      {
        if (_hasColumn(IDatabaseConstants.ID_COLUMN))
          return resultSet.getInt(IDatabaseConstants.ID_COLUMN);
        throw new BeanSQLException("The id column is not available in this result!");
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(pE);
      }
    }

    /**
     * Converts this result row to a set of field tuples based on a collection of fields, which represent the columns.
     *
     * @param pFields the fields/columns to build the tuples for
     * @return a set of field tuples (bean field + value)
     */
    public Set<FieldTuple<?>> toFieldTuples(Collection<IField<?>> pFields)
    {
      //noinspection unchecked
      return pFields.stream()
          .filter(this::hasColumn)
          .map(pField -> pField.newUntypedTuple(get(pField)))
          .collect(Collectors.toSet());
    }

    /**
     * Determines, if the result row contains a certain column.
     *
     * @param pColumnName the name of the column
     * @return <tt>true</tt>, if the column is contained
     */
    private boolean _hasColumn(String pColumnName)
    {
      try
      {
        ResultSetMetaData metadata = resultSet.getMetaData();
        return IntStream.rangeClosed(1, metadata.getColumnCount())
            .anyMatch(pIndex -> {
              try
              {
                return pColumnName.equalsIgnoreCase(metadata.getColumnName(pIndex));
              }
              catch (SQLException pE)
              {
                throw new BeanSQLException(pE);
              }
            });
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(pE);
      }
    }
  }

  /**
   * Defines a result of a select statement for a single column.
   *
   * @param <TYPE> the data type of the column/field
   */
  public class SingleColumnResult<TYPE> implements Iterable<TYPE>
  {
    private final IField<TYPE> field;
    private final Result result;

    /**
     * Create the single column result.
     *
     * @param pField  the field/column it is based on
     * @param pResult the full result of the query
     */
    public SingleColumnResult(IField<TYPE> pField, Result pResult)
    {
      field = pField;
      result = pResult;
    }

    /**
     * The value of the result from the first row.
     *
     * @return the result value of the first row
     * @throws NoResultException if, the result is empty
     */
    public TYPE getFirst() throws NoResultException
    {
      return result.getFirst()
          .orElseThrow(NoResultException::new)
          .get(field);
    }

    @NotNull
    @Override
    public Iterator<TYPE> iterator()
    {
      return new Iterator<TYPE>()
      {
        private final Iterator<ResultRow> resultIterator = result.iterator();

        @Override
        public boolean hasNext()
        {
          return resultIterator.hasNext();
        }

        @Override
        public TYPE next()
        {
          if (hasNext())
            return resultIterator.next().get(field);
          throw new NoSuchElementException();
        }
      };
    }
  }

  /**
   * A custom exception for no result queries.
   */
  public class NoResultException extends Exception
  {
  }

  /**
   * An abstract base class for select statements.
   * The statement will return a {@link Result}.
   *
   * @param <SELECT> the concrete type of the final select statement
   */
  private abstract class _AbstractSelect<SELECT extends _AbstractSelect<SELECT>> extends _AbstractStatement<_SelectModifiers, Result, SELECT>
  {
    private final List<IField<?>> fields;

    /**
     * Creates a new select statement.
     *
     * @param pFields the fields/columns that should be selected
     */
    protected _AbstractSelect(IField<?>... pFields)
    {
      super(new _SelectModifiers());
      fields = Stream.of(pFields).collect(Collectors.toList());
    }

    /**
     * Terminates the statement and return the amount of rows selected by the query.
     *
     * @return the number of rows
     */
    public int countRows()
    {
      modifiers.count = true;
      try
      {
        ResultSet resultSet = doQuery().resultSet;
        return resultSet.next() ? resultSet.getInt(IDatabaseConstants.COUNT_RESULT) : 0;
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(pE);
      }
    }

    /**
     * Determines, if the select query has a result, which means the number of rows is greater than 0.
     *
     * @return <tt>true</tt>, if the select query has a result
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
      modifiers.distinct = true;
      //noinspection unchecked
      return (SELECT) this;
    }

    @Override
    protected Result doQuery()
    {
      if (tableName == null || tableName.isEmpty())
        throw new BeanSQLException("Bad query: no table name set");

      String query = _createQuery();
      try
      {
        return new Result(connection.createStatement().executeQuery(query));
      }
      catch (SQLException pE)
      {
        throw new BeanSQLException(query, pE);
      }
    }

    /**
     * Creates the select query based on the configured information and modifiers.
     *
     * @return the select query
     */
    private String _createQuery()
    {
      return "SELECT " + modifiers.distinct() + _createColumnString() + " FROM " + tableName + modifiers.where();
    }

    /**
     * Creates the select column string for the query.
     *
     * @return the column string of the select query
     */
    private String _createColumnString()
    {
      String columns = fields.isEmpty() ? "*" :
          fields.stream()
              .map(IField::getName)
              .collect(Collectors.joining(", "));
      return modifiers.count ? "COUNT(" + columns + ") AS " + IDatabaseConstants.COUNT_RESULT : columns;
    }
  }

  /**
   * An abstract base for every statement, which is based on a table and allows conditions,
   * which determine what rows should be affected by the query.
   *
   * @param <MODIFIERS> the type of the modifiers used for this statement
   * @param <RESULT>    the result type of this statement
   * @param <STATEMENT> the concrete final type of the statement
   */
  @SuppressWarnings("unchecked")
  private abstract class _AbstractStatement<MODIFIERS extends _Modifiers, RESULT, STATEMENT extends _AbstractStatement<MODIFIERS, RESULT, STATEMENT>>
      implements IStatement
  {
    protected final MODIFIERS modifiers;
    protected String tableName;

    /**
     * Create a new statement.
     *
     * @param pModifiers the modifiers for this statement
     */
    public _AbstractStatement(MODIFIERS pModifiers)
    {
      modifiers = pModifiers;
    }

    /**
     * Executes the query and returns the result.
     *
     * @return the result of the query
     */
    protected abstract RESULT doQuery();

    /**
     * Determines, on which database table this statement should be executed.
     *
     * @param pTableName the name of the database table
     * @return the statement itself to enable a pipelining mechanism
     */
    public STATEMENT from(String pTableName)
    {
      tableName = pTableName;
      return (STATEMENT) this;
    }

    /**
     * Determines, a where condition to only affect certain rows by this statement.
     *
     * @param pTupels a collection of field tuples (bean field + value), that determine the where conditions of the statement
     * @return the statement itself to enable a pipelining mechanism
     */
    public STATEMENT where(FieldTuple<?>... pTupels)
    {
      modifiers.whereConditions.addAll(Arrays.asList(pTupels));
      return (STATEMENT) this;
    }

    /**
     * Configures the statement to only affect one row with a certain row id.
     *
     * @param pId the row id
     * @return the statement itself to enable a pipelining mechanism
     */
    public STATEMENT whereId(int pId)
    {
      modifiers.id = pId;
      return (STATEMENT) this;
    }
  }

  /**
   * The modifiers for a select statement.
   * It provides methods to build query strings based on the different modifiers.
   */
  private class _SelectModifiers extends _Modifiers
  {
    private boolean distinct = false;
    private boolean count = false;

    public String distinct()
    {
      return distinct ? "DISTINCT " : "";
    }
  }

  /**
   * The modifiers for every statement, which is based on {@link _AbstractStatement}.
   * It provides methods to build query strings based on the different modifiers.
   */
  private class _Modifiers
  {
    protected int id = -1;
    protected final Set<FieldTuple<?>> whereConditions = new HashSet<>();

    public String where()
    {
      if (id == -1 && whereConditions.isEmpty())
        return "";

      return " WHERE " + (id != -1 ? IDatabaseConstants.ID_COLUMN + " = " + id + " " : "") +
          whereConditions.stream()
              .map(pCondition -> pCondition.getField().getName() + " = " + pCondition.getValue())
              .collect(Collectors.joining(" AND "));
    }
  }
}
