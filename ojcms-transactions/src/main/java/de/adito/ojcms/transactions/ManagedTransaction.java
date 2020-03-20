package de.adito.ojcms.transactions;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

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
  private TransactionalChanges transactionalChanges;

  private final Map<String, Integer> containerSizes = new HashMap<>();
  private final Map<InitialIndexKey, PersistentBeanData> containerBeanData = new HashMap<>();
  private final Map<InitialIndexKey, Class<? extends IBean>> beanTypesWithinContainer = new HashMap<>();
  private final Map<_RequestByIdentifiers, Optional<InitialIndexKey>> identifierRequestCache = new HashMap<>();
  private final Map<SingleBeanKey, PersistentBeanData> singleBeanData = new HashMap<>();
  private final Map<String, Map<Integer, PersistentBeanData>> fullContainerData = new HashMap<>();

  @Override
  public int requestContainerSize(String pContainerId)
  {
    overallTransactionalChanges.throwIfContainerDirtyInSize(pContainerId, transactionalChanges);
    final int initialSize = containerSizes.computeIfAbsent(pContainerId, loader::loadContainerSize);
    final int differenceByChanges = transactionalChanges.getContainerSizeDifference(pContainerId);
    return initialSize + differenceByChanges;
  }

  @Override
  public PersistentBeanData requestBeanDataByIndex(CurrentIndexKey pKey)
  {
    final InitialIndexKey initialKey = transactionalChanges.currentToInitialIndexKey(pKey);
    overallTransactionalChanges.throwIfContainerBeanDirty(initialKey, transactionalChanges);

    if (transactionalChanges.isAdded(pKey))
      throw new IllegalStateException("Cannot request bean data that has just been added within this transaction!");

    if (transactionalChanges.isRemoved(initialKey))
      throw new IllegalStateException("Cannot request bean data that has been removed within this transaction!");

    final PersistentBeanData beanData = containerBeanData.computeIfAbsent(initialKey, loader::loadContainerBeanDataByIndex);
    return transactionalChanges.integrateContainerBeanChanges(initialKey, beanData);
  }

  @Override
  public <BEAN extends IBean> Class<BEAN> requestBeanTypeWithinContainer(CurrentIndexKey pKey)
  {
    final InitialIndexKey initialKey = transactionalChanges.currentToInitialIndexKey(pKey);
    overallTransactionalChanges.throwIfContainerBeanDirty(initialKey, transactionalChanges);

    if (transactionalChanges.isAdded(pKey))
      throw new IllegalStateException("Cannot request the type of a bean that has just been added within this transaction!");

    if (transactionalChanges.isRemoved(initialKey))
      throw new IllegalStateException("Cannot request the type of a bean that has been removed within this transaction!");

    //noinspection unchecked
    return (Class<BEAN>) beanTypesWithinContainer.computeIfAbsent(initialKey, loader::loadBeanTypeWithinContainer);
  }

  @Override
  public Optional<PersistentBeanData> requestBeanDataByIdentifierTuples(String pContainerId, Map<IField<?>, Object> pIdentifiers)
  {
    return identifierRequestCache.computeIfAbsent(new _RequestByIdentifiers(pContainerId, pIdentifiers), pRequest ->
    {
      //Load data by identifiers if not cached yet
      final Optional<PersistentBeanData> beanData = loader.loadContainerBeanDataByIdentifiers(pContainerId, pIdentifiers);

      if (beanData.isPresent())
      {
        final InitialIndexKey key = new InitialIndexKey(pContainerId, beanData.get().getIndex());
        containerBeanData.putIfAbsent(key, beanData.get());
        return Optional.of(key);
      }

      return Optional.empty();
    })
        //Integrate changes finally
        .map(pIndexKey -> transactionalChanges.integrateContainerBeanChanges(pIndexKey, containerBeanData.get(pIndexKey)));
  }

  @Override
  public PersistentBeanData requestSingleBeanData(SingleBeanKey pKey)
  {
    overallTransactionalChanges.throwIfSingleBeanDirty(pKey, transactionalChanges);
    final PersistentBeanData beanData = singleBeanData.computeIfAbsent(pKey, loader::loadSingleBeanData);
    return transactionalChanges.integrateSingleBeanChanges(pKey, beanData);
  }

  @Override
  public Map<Integer, PersistentBeanData> requestFullContainerLoad(String pContainerId)
  {
    overallTransactionalChanges.throwIfContainerDirtyInSize(pContainerId, transactionalChanges);
    final Map<Integer, PersistentBeanData> fullData = fullContainerData.computeIfAbsent(pContainerId, loader::fullContainerLoad);
    //Integrate changes here as well
    return fullData.entrySet().stream() //
        .collect(Collectors.toMap(Map.Entry::getKey, pEntry -> transactionalChanges
            .integrateContainerBeanChanges(new InitialIndexKey(pContainerId, pEntry.getKey()), pEntry.getValue())));
  }

  @Override
  public void registerBeanAddition(BeanAddition pBeanAddition)
  {
    overallTransactionalChanges.throwIfContainerDirtyInSize(pBeanAddition.getContainerId(), transactionalChanges);
    transactionalChanges.beanAdded(pBeanAddition);
  }

  @Override
  public void registerBeanRemoval(CurrentIndexKey pKey)
  {
    overallTransactionalChanges.throwIfContainerDirtyInSize(pKey.getContainerId(), transactionalChanges);
    transactionalChanges.beanRemoved(pKey);
  }

  @Override
  public <VALUE> void registerContainerBeanValueChange(CurrentIndexKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    final InitialIndexKey initialKey = transactionalChanges.currentToInitialIndexKey(pKey);
    overallTransactionalChanges.throwIfContainerBeanDirty(initialKey, transactionalChanges);
    transactionalChanges.containerBeanValueHasChanged(pKey, pChangedField, pNewValue);
  }

  @Override
  public <VALUE> void registerSingleBeanValueChange(SingleBeanKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    overallTransactionalChanges.throwIfSingleBeanDirty(pKey, transactionalChanges);
    transactionalChanges.singleBeanValueHasChanged(pKey, pChangedField, pNewValue);
  }

  /**
   * Commits all changes of this transaction to a persistent storage system.
   */
  void commit()
  {
    transactionalChanges.commitChanges();
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
   * Identifier and wrapper for a bean data request by identifiers (field value tuples)
   */
  private static class _RequestByIdentifiers
  {
    private final String containerId;
    private final Map<IField<?>, Object> identifiers;

    _RequestByIdentifiers(String pContainerId, Map<IField<?>, Object> pIdentifiers)
    {
      containerId = pContainerId;
      identifiers = new HashMap<>(pIdentifiers);
    }

    @Override
    public boolean equals(Object pOther)
    {
      if (this == pOther)
        return true;
      if (pOther == null || getClass() != pOther.getClass())
        return false;

      final _RequestByIdentifiers that = (_RequestByIdentifiers) pOther;
      return Objects.equals(containerId, that.containerId) && Objects.equals(identifiers, that.identifiers);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(containerId, identifiers);
    }
  }
}
