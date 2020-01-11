package de.adito.ojcms.transactions;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataStorage;

import javax.annotation.*;
import javax.inject.Inject;
import java.util.*;

/**
 * Manages bean related changes for a single transaction.
 *
 * @author Simon Danner, 27.12.2019
 */
@TransactionalScoped
class TransactionalChanges
{
  @Inject
  private OverallTransactionalChanges overallTransactionalChanges;
  @Inject
  private IBeanDataStorage storage;

  private final Map<IBeanKey, Map<IField<?>, Object>> changedBeanValues = new HashMap<>();
  private final Map<String, List<PersistentBeanData>> beanAdditions = new HashMap<>();
  private final Set<IContainerBeanKey> beanRemovals = new HashSet<>();

  /**
   * Evaluates the size difference of a bean container within the active transaction. This methods simply counts all additions
   * of the requested container and subtracts the removals from the resulting value.
   *
   * @param pContainerId the id of the container to evaluate the size difference for
   * @return zero for no size difference, a positive value for the number to increase the original size with,
   * a negative value to subtract from the original value
   */
  int getContainerSizeDifference(String pContainerId)
  {
    final int additions = beanAdditions.entrySet().stream()
        .filter(pEntry -> Objects.equals(pContainerId, pEntry.getKey()))
        .mapToInt(pEntry -> pEntry.getValue().size())
        .sum();

    final int removals = (int) beanRemovals.stream()
        .map(IBeanKey::getContainerId)
        .filter(pId -> Objects.equals(pContainerId, pId))
        .count();

    return additions - removals;
  }

  /**
   * Resolves the potentially changed values of a bean.
   *
   * @param pKey key to identify the bean to check
   * @return the optionally changed bean values
   */
  <KEY extends IBeanKey> Optional<Map<IField<?>, Object>> getPotentiallyChangedValues(KEY pKey)
  {
    return Optional.ofNullable(changedBeanValues.get(pKey))
        .map(HashMap::new);
  }

  /**
   * Determines if a bean container is changed related to its size.
   *
   * @param pContainerId the id of the container to check
   * @return <tt>true</tt> if the container has been changed related to its size
   */
  boolean isContainerDirty(String pContainerId)
  {
    return beanAdditions.containsKey(pContainerId) ||
        beanRemovals.stream().anyMatch(pKey -> Objects.equals(pKey.getContainerId(), pContainerId));
  }

  /**
   * Determines if specific bean data has been changed.
   *
   * @param pKey the key  identifying the bean data
   * @return <tt>true</tt> if the bean data has been changed
   */
  <KEY extends IBeanKey> boolean isBeanDirty(KEY pKey)
  {
    return changedBeanValues.containsKey(pKey);
  }

  /**
   * Notifies the change manager that a value of a bean has been changed.
   *
   * @param pKey          the key identifying the bean data
   * @param pChangedField the changed bean field
   * @param pNewValue     the new field value
   * @param <VALUE>       the data type of the changed field
   */
  <KEY extends IBeanKey, VALUE> void beanValueChanged(KEY pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    changedBeanValues.computeIfAbsent(pKey, key -> new HashMap<>()).put(pChangedField, pNewValue);
  }

  /**
   * Notifies the changed manager that a bean has been added to a container.
   *
   * @param pContainerId the of the container
   * @param pIndex       the index the bean has been added at
   * @param pBeanData    the data of the added bean
   */
  void beanAdded(String pContainerId, int pIndex, Map<IField<?>, Object> pBeanData)
  {
    final PersistentBeanData newBeanData = new PersistentBeanData(pIndex, pBeanData);
    beanAdditions.computeIfAbsent(pContainerId, pId -> new ArrayList<>()).add(newBeanData);
  }

  /**
   * Notifies the change manager that a bean has been removed from a container.
   *
   * @param pKey the key to identify the removed bean
   */
  <KEY extends IContainerBeanKey> void beanRemoved(KEY pKey)
  {
    beanRemovals.add(pKey);
  }


  /**
   * Commits all changes made in this transaction to the persistent storage system.
   */
  void commitChanges()
  {
    changedBeanValues.forEach((pKey, pValues) -> storage.processChangesForBean(pKey, pValues));
    beanAdditions.forEach((pContainerId, pNewData) -> storage.processAdditionsForContainer(pContainerId, pNewData));
    storage.processRemovals(beanRemovals);
  }

  /**
   * Registers this transactional changes instance at {@link OverallTransactionalChanges}.
   */
  @PostConstruct
  private void _register()
  {
    overallTransactionalChanges.registerFromTransaction(this);
  }

  /**
   * Deregisters this transactional changes instance from {@link OverallTransactionalChanges}.
   */
  @PreDestroy
  private void _deregister()
  {
    overallTransactionalChanges.deregister(this);
  }
}
