package de.adito.ojcms.transactions.api;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.Transactional;

import java.util.*;

/**
 * Defines operations from the bean context that relate to a transaction. Persistent bean data must always be accessed
 * through this interface to enable transactional features. Also changes to that data during the transaction
 * must be registered through this interface.
 *
 * If requested data has been changed by another active transaction a runtime exception will be thrown. The transactional concept retries
 * the operation in such cases. The amount of retries can be configured via {@link Transactional#tries()}
 *
 * @author Simon Danner, 26.12.2019
 */
public interface ITransaction
{
  /**
   * Requests the size of a specific persistent container.
   *
   * @param pContainerId the id of the container
   * @return the amount of beans in the container
   */
  int requestContainerSize(String pContainerId);

  /**
   * Requests persistent data of a bean within a container by index.
   *
   * @param pKey the key to identify the requested bean by index
   * @return the requested persistent bean data
   */
  PersistentBeanData requestBeanDataByIndex(CurrentIndexKey pKey);

  /**
   * Requests the type of a bean within a persistent container at a specific index.
   * This may be necessary if the type of the container is a bean base type and the actual types are persisted in the storage system.
   *
   * @param pKey the index based key to identify the bean to resolve the type for
   * @return the requested bean type
   */
  <BEAN extends IBean> Class<BEAN> requestBeanTypeWithinContainer(CurrentIndexKey pKey);

  /**
   * Requests persistent data of a bean within a container by identifying field value tuples.
   * This result may be empty if there's no bean for the given identifiers.
   *
   * @param pContainerId the id of the container the bean is located in
   * @param pIdentifiers the field value tuples to identify a bean as a map
   * @return the requested persistent bean data or empty if not found
   */
  Optional<PersistentBeanData> requestBeanDataByIdentifierTuples(String pContainerId, Map<IField<?>, Object> pIdentifiers);

  /**
   * Requests persistent data of a single bean.
   *
   * @param pKey the key to identify the requested single bean
   * @return the requested persistent bean data
   */
  PersistentBeanData requestSingleBeanData(SingleBeanKey pKey);

  /**
   * Requests a full container load that provides all persistent bean data. This method mainly exists due to a performance issue
   * to enable a way to load mass data in one call from a database system for example.
   *
   * @param pContainerId the id of the container to perform the full load for
   * @return a map containing all bean data mapped by index
   */
  Map<Integer, PersistentBeanData> requestFullContainerLoad(String pContainerId);

  /**
   * Registers the addition of a bean to a container within this transaction.
   *
   * @param pBeanAddition data describing the addition
   */
  void registerBeanAddition(BeanAddition pBeanAddition);

  /**
   * Registers the removal of a bean from a container within this transaction.
   *
   * @param pContainerKey the index based key to identify the removed bean
   */
  void registerBeanRemoval(CurrentIndexKey pContainerKey);

  /**
   * Registers a value change of a persistent bean within this transaction.
   *
   * @param pKey          the key to identify the changed bean by index
   * @param pChangedField the changed bean field
   * @param pNewValue     the new value for the field
   * @param <VALUE>       the value type of the changed field
   */
  <VALUE> void registerContainerBeanValueChange(CurrentIndexKey pKey, IField<VALUE> pChangedField, VALUE pNewValue);

  /**
   * Registers a value change of a persistent single bean within this transaction.
   *
   * @param pKey          the key to identify the changed single bean
   * @param pChangedField the changed bean field
   * @param pNewValue     the new value for the field
   * @param <VALUE>       the value type of the changed field
   */
  <VALUE> void registerSingleBeanValueChange(SingleBeanKey pKey, IField<VALUE> pChangedField, VALUE pNewValue);
}
