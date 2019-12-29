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

  private final Map<ContainerIndexKey, Map<IField<?>, Object>> changedBeanValues = new HashMap<>();
  private final Map<String, Map<IField<?>, Object>> changedSingleBeanValues = new HashMap<>();
  private final Map<String, List<BeanData<ContainerIndexKey>>> beanAdditions = new HashMap<>();
  private final Set<ContainerIndexKey> removalsById = new HashSet<>();
  private final Set<ContainerIdentifierKey> removalsByIdentifiers = new HashSet<>();

  /**
   * Determines if a bean container is changed related to its size.
   *
   * @param pContainerId the id of the container to check
   * @return <tt>true</tt> if the container has been changed related to its size
   */
  boolean isContainerDirty(String pContainerId)
  {
    return beanAdditions.containsKey(pContainerId) ||
        removalsById.stream().anyMatch(pKey -> Objects.equals(pKey.getContainerId(), pContainerId)) ||
        removalsByIdentifiers.stream().anyMatch(pKey -> Objects.equals(pKey.getContainerId(), pContainerId));
  }

  /**
   * Determines if specific bean data within a container has been changed.
   *
   * @param pKey the index based key identifying the bean data
   * @return <tt>true</tt> if the bean data has been changed
   */
  boolean isBeanInContainerDirty(ContainerIndexKey pKey)
  {
    return changedBeanValues.containsKey(pKey);
  }

  /**
   * Determines if single bean data has been changed.
   *
   * @param pKey the single bean id
   * @return <tt>true</tt> if the single bean data has been changed
   */
  boolean isSingleBeanDirty(String pKey)
  {
    return changedSingleBeanValues.containsKey(pKey);
  }

  /**
   * Notifies the change manager that a value of a bean in a container has been changed.
   *
   * @param pKey          the index based key identifying the bean data in the container
   * @param pChangedField the changed bean field
   * @param pNewValue     the new field value
   * @param <VALUE>       the data type of the changed field
   */
  <VALUE> void beanValueChanged(ContainerIndexKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    changedBeanValues.computeIfAbsent(pKey, key -> new HashMap<>()).put(pChangedField, pNewValue);
  }

  /**
   * Notifies the change manager that a value of a single bean has been changed.
   *
   * @param pKey          the id of the changed single bean
   * @param pChangedField the changed bean field
   * @param pNewValue     the new field value
   * @param <VALUE>       the data type of the changed field
   */
  <VALUE> void singleBeanValueChanged(String pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    changedSingleBeanValues.computeIfAbsent(pKey, key -> new HashMap<>()).put(pChangedField, pNewValue);
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
    final BeanData<ContainerIndexKey> newBeanData = new BeanData<>(new ContainerIndexKey(pContainerId, pIndex), pBeanData);
    beanAdditions.computeIfAbsent(pContainerId, pId -> new ArrayList<>()).add(newBeanData);
  }

  /**
   * Notifies the change manager that a bean has been removed from a container.
   *
   * @param pKey the index based key to identify the removed bean
   */
  void beanRemoved(ContainerIndexKey pKey)
  {
    removalsById.add(pKey);
  }

  /**
   * Notifies the change manager that a bean has been removed from a container.
   *
   * @param pKey the identifier fields based key to identify the removed bean
   */
  void beanRemoved(ContainerIdentifierKey pKey)
  {
    removalsByIdentifiers.add(pKey);
  }

  /**
   * Commits all changes made in this transaction to the persistent storage system.
   */
  void commitChanges()
  {
    changedBeanValues.forEach((pKey, pValues) -> storage.processChangesForBean(pKey, pValues));
    changedSingleBeanValues.forEach((pKey, pValues) -> storage.processChangesForSingleBean(pKey, pValues));
    beanAdditions.forEach((pContainerId, pNewData) -> storage.processAdditionsForContainer(pContainerId, pNewData));
    storage.processRemovalsById(removalsById);
    storage.processRemovalsByIdentifiers(removalsByIdentifiers);
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
