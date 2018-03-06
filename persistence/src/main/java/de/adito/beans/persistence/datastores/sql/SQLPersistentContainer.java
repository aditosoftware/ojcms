package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.util.*;
import de.adito.beans.persistence.BeanPersistenceUtil;
import de.adito.beans.persistence.datastores.sql.util.*;
import de.adito.beans.persistence.spi.*;
import org.jetbrains.annotations.*;

import java.sql.*;
import java.util.*;
import java.util.function.*;

/**
 * Implementation of a persistent bean container based on a SQL database system.
 * It defines a builder for the data core of a bean container. ({@link IBeanContainerEncapsulated})
 * This means the data comes from the database directly,
 * but the bean container interface ({@link IBeanContainer}) is used like with normal data cores.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 18.02.2018
 */
public class SQLPersistentContainer<BEAN extends IBean<BEAN>> implements IPersistentBeanContainer<BEAN>
{
  private final Class<BEAN> beanType;
  private final List<IField<?>> fields;
  private final ESupportedSQLTypes type;
  private final int port;
  private final String host, tableName, databaseName;
  @Nullable
  private final String userName, password; //optional
  private final Map<Integer, BEAN> beanCache = new HashMap<>();

  /**
   * Creates a new persistent bean container.
   *
   * @param pBeanType     the type of the beans in the container
   * @param pType         the database type
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @param pTableName    the name of the database table that represents this container core
   */
  public SQLPersistentContainer(Class<BEAN> pBeanType, ESupportedSQLTypes pType, String pHost, int pPort, String pDatabaseName, String pTableName)
  {
    this(pBeanType, pType, pHost, pPort, pDatabaseName, null, null, pTableName);
  }

  /**
   * Creates a new persistent bean container.
   *
   * @param pBeanType     the type of the beans in the container
   * @param pType         the database type
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @param pTableName    the name of the database table that represents this container core
   * @param pUserName     an optional user name for the connection
   * @param pPassword     an optional password for the connection
   */
  public SQLPersistentContainer(Class<BEAN> pBeanType, ESupportedSQLTypes pType, String pHost, int pPort, String pDatabaseName,
                                @Nullable String pUserName, @Nullable String pPassword, String pTableName)
  {
    beanType = pBeanType;
    fields = BeanReflector.reflectBeanFields(beanType);
    type = pType;
    host = pHost;
    port = pPort;
    databaseName = pDatabaseName;
    userName = pUserName;
    password = pPassword;
    tableName = pTableName;
    //Setup driver
    try
    {
      Class.forName(type.getDriverName());
    }
    catch (ClassNotFoundException pE)
    {
      throw new RuntimeException("Driver '" + type.getDriverName() + "' not found!", pE);
    }
    //Create table if necessary
    _checkTable();
  }

  @Override
  public void addBean(BEAN pBean, int pIndex)
  {
    _insert(pInsert -> pInsert
        .atIndex(pIndex)
        .values(pBean.stream().toArray(FieldTuple[]::new))
        .insert());
    beanCache.put(pIndex, _injectPersistentCore(pBean, pIndex));
  }

  @Override
  public boolean removeBean(BEAN pBean)
  {
    int index = indexOfBean(pBean);
    if (index == -1)
      return false;
    _removeByIndex(index);
    return true;
  }

  @Override
  public BEAN removeBean(int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);
    //Inject the default encapsulated core with the values from the database to allow the bean to exist after the removal
    BEAN removed = EncapsulatedBuilder.injectDefaultEncapsulated(getBean(pIndex));
    _removeByIndex(pIndex);
    return removed;
  }

  @Override
  public boolean containsBean(BEAN pBean)
  {
    return beanCache.containsValue(pBean) || _selectAll(pSelect -> pSelect
        .where(_beanIdentifiersToTuples(pBean))
        .hasResult());
  }

  @Override
  public BEAN getBean(int pIndex)
  {
    return beanCache.computeIfAbsent(pIndex, pCreationIndex -> _injectPersistentCore(BeanPersistenceUtil.newInstance(beanType), pCreationIndex));
  }

  @Override
  public int indexOfBean(BEAN pBean)
  {
    return beanCache.entrySet().stream()
        .filter(pEntry -> Objects.equals(pBean, pEntry.getValue()))
        .findFirst()
        .map(Map.Entry::getKey)
        .orElse(_selectAll(pSelect -> pSelect
            .where(_beanIdentifiersToTuples(pBean))
            .firstResult()
            .map(SQLBuilder.ResultRow::getIdIfAvailable)
            .orElse(-1)));
  }

  @Override
  public int size()
  {
    return _selectAll(SQLBuilder.Select::countRows);
  }

  @NotNull
  @Override
  public Iterator<BEAN> iterator()
  {
    return new IndexBasedIterator<>(size(), this::getBean);
  }

  /**
   * Checks, if the table exists in the database and creates it if necessary
   */
  private void _checkTable()
  {
    try (Connection connection = _connect())
    {
      if (!connection.getMetaData().getTables(null, null, tableName, null).next())
        _create(pCreate -> pCreate
            .withIdColumn()
            .columns(fields)
            .create());
    }
    catch (SQLException pE)
    {
      throw new BeanSQLException(pE);
    }
  }

  /**
   * The bean identifier field tuples as array.
   *
   * @param pBean the bean from which the identifiers should be returned
   * @return an array of field tuples
   */
  private FieldTuple<?>[] _beanIdentifiersToTuples(IBean<?> pBean)
  {
    //noinspection unchecked,SimplifyStreamApiCallChains
    return pBean.getIdentifiers().stream()
        .toArray(FieldTuple[]::new);
  }

  /**
   * Injects a database based data core into a bean instance.
   *
   * @param pInstance the bean instance to inject to data core
   * @param pIndex    the index of the bean within the container
   * @return the bean instance
   */
  private BEAN _injectPersistentCore(BEAN pInstance, int pIndex)
  {
    return EncapsulatedBuilder.injectCustomEncapsulated(pInstance, new _ContainerBean(pIndex));
  }

  /**
   * Removes a bean at a certain index.
   * The bean instance will also be removed from the cache.
   *
   * @param pIndex the index to remove to bean from
   */
  private void _removeByIndex(int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("index: " + pIndex);

    boolean deleted = _delete(pDelete -> pDelete
        .whereId(pIndex)
        .delete());

    if (!deleted)
      throw new BeanSQLException("Unexpected SQL error while removing bean from container!");

    beanCache.remove(pIndex);
  }

  /**
   * Performs a select-all query on the given database table.
   * This method has to be provided with a function, which returns a certain result based on the select query.
   * The result must be evaluated within this function, because the connection will be closed automatically afterwards.
   *
   * @param pSelect  a function that returns a result based on the select query (see {@link SQLBuilder.Select})
   * @param <RESULT> the type of the result
   * @return the result determined by the given function
   */
  private <RESULT> RESULT _selectAll(Function<SQLBuilder.Select, RESULT> pSelect)
  {
    return _queryAndClose(pBuilder -> pBuilder.select().from(tableName), pSelect);
  }

  /**
   * Performs a single column select (one bean field) on the given database table.
   * This method has to be provided with a function, which returns a certain result based on the select query.
   * The result must be evaluated within this function, because the connection will be closed automatically afterwards.
   *
   * @param pField   the column from which the value should be queried
   * @param pSelect  a function that returns a result based on the select query (see {@link SQLBuilder.SingleSelect})
   * @param <TYPE>   the data type of the bean field
   * @param <RESULT> the type of the result
   * @return the result determined by the given function
   */
  private <TYPE, RESULT> RESULT _selectOne(IField<TYPE> pField, Function<SQLBuilder.SingleSelect<TYPE>, RESULT> pSelect)
  {
    return _queryAndClose(pBuilder -> pBuilder.selectOne(pField).from(tableName), pSelect);
  }

  /**
   * Performs a create query of the given table.
   * This method has to be provided with a consumer of the predefined create query,
   * because the connection will be closed automatically afterwards.
   *
   * @param pCreate the create query as consumer of the predefined query
   */
  private void _create(Consumer<SQLBuilder.Create> pCreate)
  {
    _executeAndClose(pBuilder -> pBuilder.create(tableName), pCreate);
  }

  /**
   * Performs a insert query on the given database table.
   * This method has to be provided with a consumer of the predefined insert query,
   * because the connection will be closed automatically afterwards.
   *
   * @param pInsert the insert query as consumer of the predefined query
   */
  private void _insert(Consumer<SQLBuilder.Insert> pInsert)
  {
    _executeAndClose(pBuilder -> pBuilder.insert().into(tableName), pInsert);
  }

  /**
   * Performs a update query on the given database table.
   * This method has to be provided with a consumer of the predefined update query,
   * because the connection will be closed automatically afterwards.
   *
   * @param pUpdate the update query as consumer of the predefined query
   */
  private void _update(Consumer<SQLBuilder.Update> pUpdate)
  {
    _executeAndClose(pBuilder -> pBuilder.update().from(tableName), pUpdate);
  }

  /**
   * Performs a delete query on the given database table.
   * This method has to be provided with a consumer of the predefined delete query,
   * because the connection will be closed automatically afterwards.
   *
   * @param pDelete the delete query as consumer of the predefined query
   */
  private boolean _delete(Function<SQLBuilder.Delete, Boolean> pDelete)
  {
    return _queryAndClose(pBuilder -> pBuilder.delete().from(tableName), pDelete);
  }

  /**
   * Performs a no result database query and closes the connection afterwards.
   * The database queries will be created and adapted by {@link SQLBuilder}.
   *
   * @param pQueryResolver a function that creates the initial query based on a query builder
   * @param pQuery         a consumer of the specific query, that will be executed on a new connection, which will be closed afterwards
   * @param <QUERY>        the type of the specific query created by the query builder
   */
  private <QUERY extends SQLBuilder.IStatement> void _executeAndClose(Function<SQLBuilder, QUERY> pQueryResolver, Consumer<QUERY> pQuery)
  {
    try (Connection connection = _connect())
    {
      pQuery.accept(pQueryResolver.apply(new SQLBuilder(connection)));
    }
    catch (SQLException pE)
    {
      throw new RuntimeException("Unable to close database connection!", pE);
    }
  }

  /**
   * Performs a database query with a certain result and closes the connection afterwards.
   * The database queries will be created and adapted by {@link SQLBuilder}.
   *
   * @param pQueryResolver a function that creates the initial query based on a query builder
   * @param pQuery         a function that will be provided with the query and that should return the result
   * @param <QUERY>        the type of the specific query created by the query builder
   * @param <RESULT>       the type of the result
   * @return the result determined by the given function
   */
  private <QUERY extends SQLBuilder.IStatement, RESULT> RESULT _queryAndClose(Function<SQLBuilder, QUERY> pQueryResolver,
                                                                              Function<QUERY, RESULT> pQuery)
  {
    try (Connection connection = _connect())
    {
      return pQuery.apply(pQueryResolver.apply(new SQLBuilder(connection)));
    }
    catch (SQLException pE)
    {
      throw new RuntimeException("Unable to close database connection!", pE);
    }
  }

  /**
   * Establishes a new connection to the database.
   *
   * @return the established connection
   */
  private Connection _connect()
  {
    String dbUrl = type.getConnectionString(host, port, databaseName);
    try
    {
      return userName == null || password == null ? DriverManager.getConnection(dbUrl) : DriverManager.getConnection(dbUrl, userName, password);
    }
    catch (SQLException pE)
    {
      throw new RuntimeException("Unable to connect to database! host = " + host + " port = " + port, pE);
    }
  }

  /**
   * The persistent bean implementation of this container.
   * The queries within this bean core are based on the index of the bean.
   */
  private class _ContainerBean implements IPersistentBean
  {
    private final int index;

    private _ContainerBean(int pIndex)
    {
      index = pIndex;
    }

    @Override
    public <TYPE> TYPE getValue(IField<TYPE> pField)
    {
      return _selectOne(pField, pSelect -> {
        try
        {
          return pSelect
              .whereId(index)
              .firstResult();
        }
        catch (SQLBuilder.NoResultException pE)
        {
          throw new BeanSQLException("Unexpected: No result for index " + index + " found. field: " + pField);
        }
      });
    }

    @Override
    public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue, boolean pAllowNewField)
    {
      _update(pUpdate -> pUpdate
          .set(new FieldTuple<>(pField, pValue))
          .update());
    }

    @Override
    public <TYPE> void removeField(IField<TYPE> pField)
    {
      //Keep the column
    }

    @NotNull
    @Override
    public Iterator<FieldTuple<?>> iterator()
    {
      final Set<FieldTuple<?>> tuples = _selectAll(pSelect -> pSelect
          .whereId(index)
          .firstResult()
          .map(pRow -> pRow.toFieldTuples(fields))
          .orElseThrow(() -> new BeanSQLException("Unexpected: No result with ID " + index + " found.")));

      //If a tuple is not given by the query result, add it with a null value
      return new IndexBasedIterator<>(fields.size(), pIndex -> tuples.stream()
          .filter(pTuple -> pTuple.getField() == fields.get(pIndex))
          .findAny()
          .orElse(new FieldTuple<>(fields.get(pIndex), null)));
    }
  }
}
