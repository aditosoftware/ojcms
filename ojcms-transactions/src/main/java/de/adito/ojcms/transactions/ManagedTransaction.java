package de.adito.ojcms.transactions;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.*;

import javax.inject.Inject;
import java.util.*;

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
  private IBeanDataStorage storage;
  @Inject
  private OverallTransactionalChanges overallTransactionalChanges;
  @Inject
  private TransactionalChanges changes;

  private final Map<String, Integer> containerSizeCache = new HashMap<>();
  private final Map<IBeanKey, PersistentBeanData> beanDataCache = new HashMap<>();
  private final Map<String, Map<Integer, PersistentBeanData>> fullContainerLoadCache = new HashMap<>();

  @Override
  public int requestContainerSize(String pContainerId)
  {
    overallTransactionalChanges.throwIfContainerDirty(pContainerId, changes);

    final int initialSize = containerSizeCache.computeIfAbsent(pContainerId, loader::loadContainerSize);
    final int differenceByChanges = changes.getContainerSizeDifference(pContainerId);
    return initialSize + differenceByChanges;
  }

  @Override
  public <KEY extends IBeanKey> PersistentBeanData requestBeanDataByKey(KEY pKey)
  {
    overallTransactionalChanges.throwIfBeanDirty(pKey, changes);

    final PersistentBeanData initialData = beanDataCache.computeIfAbsent(pKey, loader::loadByKey);
    return _integrateChanges(pKey, initialData);
  }

  @Override
  public Map<Integer, PersistentBeanData> requestFullContainerLoad(String pContainerId)
  {
    overallTransactionalChanges.throwIfContainerDirty(pContainerId, changes);
    final Map<Integer, PersistentBeanData> fullData = fullContainerLoadCache.computeIfAbsent(pContainerId, loader::fullContainerLoad);

    for (PersistentBeanData beanData : fullData.values())
    {
      //There may be changes registered by index or identifiers
      _integrateChanges(beanData.createIndexKey(pContainerId), beanData);
      _integrateChanges(beanData.createIdentifierKey(pContainerId), beanData);
    }

    return fullData;
  }

  @Override
  public void registerBeanAddition(String pContainerId, int pIndex, Map<IField<?>, Object> pNewData)
  {
    overallTransactionalChanges.throwIfContainerDirty(pContainerId, changes);
    changes.beanAdded(pContainerId, pIndex, pNewData);
  }

  @Override
  public <KEY extends IContainerBeanKey> void registerBeanRemoval(KEY pKey)
  {
    overallTransactionalChanges.throwIfContainerDirty(pKey.getContainerId(), changes);
    changes.beanRemoved(pKey);
  }

  @Override
  public <KEY extends IBeanKey, VALUE> void registerBeanValueChange(KEY pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    overallTransactionalChanges.throwIfBeanDirty(pKey, changes);
    changes.beanValueChanged(pKey, pChangedField, pNewValue);
  }

  /**
   * Commits all changes of this transaction to a persistent storage system.
   */
  void commit()
  {
    changes.commitChanges();
    storage.commitChanges();
  }

  /**
   * Rolls back all changes made during this transaction.
   */
  void rollback()
  {
    storage.rollbackChanges();
  }

  /**
   * Integrates changes made within this transaction to an instance of {@link PersistentBeanData}.
   *
   * @param pKey         the key to identify the changes
   * @param pInitialData the intial bean data to integrate the changes into
   * @return the persistent bean data with integrated changes
   */
  private <KEY extends IBeanKey> PersistentBeanData _integrateChanges(KEY pKey, PersistentBeanData pInitialData)
  {
    return changes.getPotentiallyChangedValues(pKey)
        .map(pInitialData::integrateChanges)
        .orElse(pInitialData);
  }
}
