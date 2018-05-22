package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.util.*;
import de.adito.beans.persistence.BeanPersistenceUtil;
import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.result.ResultRow;
import de.adito.beans.persistence.datastores.sql.builder.statements.Select;
import de.adito.beans.persistence.datastores.sql.builder.util.*;
import de.adito.beans.persistence.spi.*;
import org.jetbrains.annotations.*;

import java.util.*;

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
  private final List<BeanColumnIdentification<?>> columns;
  private final OJSQLBuilderForTable builder;
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
  public SQLPersistentContainer(Class<BEAN> pBeanType, EDatabaseType pType, String pHost, int pPort, String pDatabaseName, String pTableName)
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
  public SQLPersistentContainer(Class<BEAN> pBeanType, EDatabaseType pType, String pHost, int pPort, String pDatabaseName,
                                @Nullable String pUserName, @Nullable String pPassword, String pTableName)
  {
    beanType = pBeanType;
    columns = BeanColumnIdentification.of(BeanReflector.reflectBeanFields(beanType));
    builder = OJSQLBuilderFactory.newSQLBuilder(pType, IDatabaseConstants.ID_COLUMN)
        .forSingleTable(pTableName)
        .withClosingAndRenewingConnection(pHost, pPort, pDatabaseName, pUserName, pPassword)
        .create();
    //Setup driver
    try
    {
      Class.forName(pType.getDriverName());
    }
    catch (ClassNotFoundException pE)
    {
      throw new RuntimeException("Driver '" + pType.getDriverName() + "' not found!", pE);
    }
    //Create table if necessary
    builder.ifTableNotExistingCreate(pCreate -> pCreate
        .withIdColumn()
        .columns(BeanColumnDefinition.of(columns))
        .create());
  }

  @Override
  public void addBean(BEAN pBean, int pIndex)
  {
    builder.doInsert(pInsert -> pInsert
        .atIndex(pIndex)
        .values(BeanColumnValueTuple.of(pBean))
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
    return beanCache.containsValue(pBean) || builder.doSelect(pSelect -> pSelect
        .where(BeanColumnValueTuple.ofBeanIdentifiers(pBean))
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
        .orElse(builder.doSelect(pSelect -> pSelect
            .where(BeanColumnValueTuple.ofBeanIdentifiers(pBean))
            .firstResult()
            .map(ResultRow::getIdIfAvailable)
            .orElse(-1)));
  }

  @Override
  public int size()
  {
    return builder.doSelect(Select::countRows);
  }

  @NotNull
  @Override
  public Iterator<BEAN> iterator()
  {
    return new IndexBasedIterator<>(size(), this::getBean);
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

    boolean deleted = builder.doDelete(pDelete -> pDelete
        .whereId(pIndex)
        .delete());

    if (!deleted)
      throw new OJDatabaseException("Unexpected SQL error while removing bean from container!");

    beanCache.remove(pIndex);
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
      return builder.doSelectOne(new BeanColumnIdentification<>(pField), pSelect -> pSelect
          .whereId(index)
          .firstResult()
          .orIfNotPresentThrow(() -> new OJDatabaseException("No result for index " + index + " found. field: " + pField)));
    }

    @Override
    public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue, boolean pAllowNewField)
    {
      builder.doUpdate(pUpdate -> pUpdate
          .set(new BeanColumnValueTuple<>(pField.newTuple(pValue)))
          .whereId(index)
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
      List<FieldTuple<?>> fieldTuples = builder.doSelect(pSelect -> pSelect
          .whereId(index)
          .firstResult()
          .map(this::_mapResultToFieldTuples))
          .orElseThrow(() -> new OJDatabaseException("No result with id " + index + " found."));
      return fieldTuples.iterator();
    }

    /**
     * Transfers the result of a select query to a list of bean tuples.
     *
     * @param pResultRow the result row of the select query
     * @return a list of field value tuples
     */
    private List<FieldTuple<?>> _mapResultToFieldTuples(ResultRow pResultRow)
    {
      //noinspection unchecked
      return columns.stream()
          .map(pColumn -> ((IField) pColumn.getBeanField()).newTuple(pResultRow.hasColumn(pColumn) ? pResultRow.get(pColumn) : null))
          .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
  }
}
