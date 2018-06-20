package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.util.*;
import de.adito.beans.persistence.*;
import de.adito.beans.persistence.datastores.sql.builder.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.IWhereCondition;
import de.adito.beans.persistence.datastores.sql.builder.result.ResultRow;
import de.adito.beans.persistence.datastores.sql.builder.statements.Select;
import de.adito.beans.persistence.datastores.sql.builder.util.*;
import de.adito.beans.persistence.datastores.sql.util.*;
import de.adito.beans.persistence.spi.*;
import org.jetbrains.annotations.NotNull;

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
  private final boolean isAutomaticAdditionMode;
  private final List<BeanColumnIdentification<?>> columns;
  private final OJSQLBuilderForTable builder;
  private final Map<Integer, BEAN> beanCache = new HashMap<>();
  private final Map<BEAN, Integer> additionQueue = new HashMap<>();
  private boolean shouldQueueAdditions = false;

  /**
   * Creates a new persistent bean container.
   *
   * @param pBeanType       the type of the beans in the container
   * @param pConnectionInfo information for the database connection
   * @param pTableName      the name of the database table that represents this container core
   * @param pBeanDataStore  the data store for persistent bean elements
   */
  public SQLPersistentContainer(Class<BEAN> pBeanType, DBConnectionInfo pConnectionInfo, String pTableName, BeanDataStore pBeanDataStore)
  {
    beanType = pBeanType;
    isAutomaticAdditionMode = pBeanType.getAnnotation(Persist.class).storageMode() == EStorageMode.AUTOMATIC;
    columns = BeanColumnIdentification.ofMultiple(BeanReflector.reflectBeanFields(beanType));
    builder = OJSQLBuilderFactory.newSQLBuilder(pConnectionInfo.getDatabaseType(), IDatabaseConstants.ID_COLUMN)
        .forSingleTable(pTableName)
        .withClosingAndRenewingConnection(pConnectionInfo)
        .withCustomSerializer(new BeanSQLSerializer(pBeanDataStore))
        .create();
    //Setup driver
    try
    {
      Class.forName(pConnectionInfo.getDatabaseType().getDriverName());
    }
    catch (ClassNotFoundException pE)
    {
      throw new RuntimeException("Driver '" + pConnectionInfo.getDatabaseType().getDriverName() + "' not found!", pE);
    }
    //Create table if necessary
    builder.ifTableNotExistingCreate(pCreate -> pCreate
        .withIdColumn()
        .columns(BeanColumnDefinition.ofMultiple(columns))
        .create());
  }

  @Override
  public void addBean(BEAN pBean, int pIndex)
  {
    //Additions will be queued in the automatic mode, when a new bean instance is created at this certain time.
    //Otherwise unwanted copies will be created, because a new instance will lead to an addition in the automatic mode.
    if (isAutomaticAdditionMode)
      synchronized (additionQueue)
      {
        if (shouldQueueAdditions)
        {
          additionQueue.put(pBean, pIndex);
          return;
        }
      }
    builder.doInsert(pInsert -> pInsert
        .atIndex(pIndex)
        .values(BeanColumnValueTuple.ofBean(pBean))
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
    EncapsulatedBuilder.injectDefaultEncapsulated(pBean);
    return true;
  }

  @Override
  public BEAN removeBean(int pIndex)
  {
    //Inject the default encapsulated core with the values from the database to allow the bean to exist after the removal
    BEAN removed = EncapsulatedBuilder.injectDefaultEncapsulated(getBean(_requireInRange(pIndex)));
    _removeByIndex(pIndex);
    return removed;
  }

  @Override
  public BEAN getBean(int pIndex)
  {
    return beanCache.computeIfAbsent(_requireInRange(pIndex), pCreationIndex -> _injectPersistentCore(_createBeanInstance(), pCreationIndex));
  }

  @Override
  public int indexOfBean(BEAN pBean)
  {
    return beanCache.entrySet().stream()
        .filter(pEntry -> Objects.equals(pBean, pEntry.getValue()))
        .findFirst()
        .map(Map.Entry::getKey)
        .orElseGet(() -> {
          IWhereCondition<?>[] conditions = BeanWhereCondition.ofBeanIdentifiers(pBean);
          if (conditions.length == 0)
            throw new OJDatabaseException("A bean instance not created by this container can only be used for a index-of or contains search," +
                                              " if there are bean fields marked as @Identifier!");
          return builder.doSelect(pSelect -> pSelect
              .where(conditions)
              .firstResult()
              .map(ResultRow::getIdIfAvailable)
              .orElse(-1));
        });
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
    return new IndexBasedIterator<>(size(), this::getBean, this::removeBean);
  }

  /**
   * Makes sure an index is within the range of this container.
   * It has to be between 0 and size().
   *
   * @param pIndex the index to check
   * @return the checked index
   */
  private int _requireInRange(int pIndex)
  {
    final int size = size();
    if (pIndex < 0 || pIndex >= size)
      throw new IndexOutOfBoundsException("The index for the bean is not within the range of this container. index: " + pIndex + ", size: " + size);
    return pIndex;
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
   * Creates a new instance of a bean of this container's bean type.
   * If the type of the persistent bean is {@link EStorageMode#AUTOMATIC}, additions will be disabled for this short period of time.
   * All additions happening in the mean time, will be stored in a queue, which will be executed afterwards.
   * It is necessary to queue the additions in the automatic mode to avoid copies while creating a new instance from this class.
   *
   * @return a new instance of a bean
   */
  private BEAN _createBeanInstance()
  {
    if (!isAutomaticAdditionMode)
      return BeanPersistenceUtil.newInstance(beanType);
    shouldQueueAdditions = true;
    BEAN instance = BeanPersistenceUtil.newInstance(beanType);
    synchronized (additionQueue)
    {
      additionQueue.remove(instance);
      additionQueue.forEach(this::addBean);
      additionQueue.clear();
      shouldQueueAdditions = false;
    }
    return instance;
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
      throw new UnsupportedOperationException("It's not allowed to remove fields from a persistent bean!");
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
