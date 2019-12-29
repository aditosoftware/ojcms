package de.adito.ojcms.transactions;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataLoader;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

/**
 * Implementation of a managed transaction that lives in a transactional scope.
 * Manages the loading and storage of persistent bean data for this transaction without concurrency issues.
 * Also commits or rolls back the changes after the transaction finally.
 *
 * @author Simon Danner, 26.12.2019
 */
@TransactionalScoped
class ManagedTransaction implements ITransaction
{
  @Inject
  private IBeanDataLoader loader;
  @Inject
  private OverallTransactionalChanges overallTransactionalChanges;
  @Inject
  private TransactionalChanges changes;
  @Inject
  private Connection connection;

  private final Map<String, Integer> containerSizeCache = new HashMap<>();
  private final Map<ContainerIndexKey, BeanData<ContainerIndexKey>> byIndexCache = new HashMap<>();
  private final Map<ContainerIdentifierKey, BeanData<ContainerIndexKey>> byIdentifierCache = new HashMap<>();
  private final Map<String, BeanData<String>> singleBeanCache = new HashMap<>();

  @Override
  public int requestContainerSize(String pContainerId)
  {
    if (overallTransactionalChanges.isContainerDirty(pContainerId, changes))
      throw new ConcurrentTransactionException(pContainerId);

    return containerSizeCache.computeIfAbsent(pContainerId, loader::loadSize);
  }

  @Override
  public BeanData<ContainerIndexKey> requestBeanDataFromContainer(ContainerIndexKey pIndexBasedKey)
  {
    if (overallTransactionalChanges.isBeanInContainerDirty(pIndexBasedKey, changes))
      throw new ConcurrentTransactionException(pIndexBasedKey);

    return _retrieveFromCacheOrLoadAndCache(byIndexCache, pIndexBasedKey, loader::loadByIndex);
  }

  @Override
  public BeanData<ContainerIndexKey> requestBeanDataFromContainer(ContainerIdentifierKey pIdentifierKey)
  {
    final BeanData<ContainerIndexKey> beanData = _retrieveFromCacheOrLoadAndCache(byIdentifierCache, pIdentifierKey,
                                                                                  loader::loadByIdentifiers);

    //If there are changes to the requested bean, it must have been cached, so no problem retrieving the index key from there
    if (overallTransactionalChanges.isBeanInContainerDirty(beanData.getKey(), changes))
      throw new ConcurrentTransactionException(beanData.getKey());

    return beanData;
  }

  @Override
  public BeanData<String> requestSingleBeanData(String pSingleBeanId)
  {
    if (overallTransactionalChanges.isSingleBeanDirty(pSingleBeanId, changes))
      throw new ConcurrentTransactionException(pSingleBeanId);

    return singleBeanCache.computeIfAbsent(pSingleBeanId, loader::loadSingleBean);
  }

  @Override
  public void registerBeanAddition(String pContainerId, int pIndex, Map<IField<?>, Object> pNewData)
  {
    changes.beanAdded(pContainerId, pIndex, pNewData);
  }

  @Override
  public void registerBeanRemoval(ContainerIndexKey pIndexBasedKey)
  {
    changes.beanRemoved(pIndexBasedKey);
  }

  @Override
  public void registerBeanRemoval(ContainerIdentifierKey pIdentifierKey)
  {
    changes.beanRemoved(pIdentifierKey);
  }

  @Override
  public <VALUE> void registerBeanValueChange(ContainerIndexKey pContainerKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    changes.beanValueChanged(pContainerKey, pChangedField, pNewValue);
  }

  @Override
  public <VALUE> void registerSingleBeanValueChange(String pSingleBeanId, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    changes.singleBeanValueChanged(pSingleBeanId, pChangedField, pNewValue);
  }

  /**
   * Commits all changes of this transaction to a persistent storage system.
   */
  void commit()
  {
    changes.commitChanges();

    try
    {
      connection.commit();
    }
    catch (SQLException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * Rolls back all changes made during this transaction.
   */
  void rollback()
  {
    try
    {
      connection.rollback();
    }
    catch (SQLException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  /**
   * Tries to retrieve bean data from the cache or load the data if not cached yet.
   *
   * @param pCache the cache holding bean data by identifying keys
   * @param pKey the generic key to identify the bean data
   * @param loadingFunction a function to load the persistent bean data by the key
   * @param <KEY> the generic type of the key identifying the bean data
   * @return the cached or loaded bean data
   */
  private <KEY> BeanData<ContainerIndexKey> _retrieveFromCacheOrLoadAndCache(Map<KEY, BeanData<ContainerIndexKey>> pCache, KEY pKey,
                                                                             Function<KEY, BeanData<ContainerIndexKey>> loadingFunction)
  {
    if (pCache.containsKey(pKey))
      return pCache.get(pKey);

    final BeanData<ContainerIndexKey> beanData = loadingFunction.apply(pKey);
    byIndexCache.put(beanData.getKey(), beanData);
    byIdentifierCache.put(beanData.getIdentifierKey(beanData.getKey().getContainerId()), beanData);

    return beanData;
  }
}
