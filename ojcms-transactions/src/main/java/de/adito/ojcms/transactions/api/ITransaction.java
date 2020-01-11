package de.adito.ojcms.transactions.api;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.Transactional;

import java.util.Map;

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
   * Requests persistent data of a bean for a given key.
   * The exception handling (in case a bean cannot be found within the container etc.) must be defined by the classes implementing the SPI.
   *
   * @param pKey the key to identify the requested bean
   * @return the requested persistent bean data
   */
  <KEY extends IBeanKey> PersistentBeanData requestBeanDataByKey(KEY pKey);

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
   * @param pContainerId the id of the container the bean has been added to
   * @param pIndex       the index of the added bean
   * @param pNewData     the data of the added bean
   */
  void registerBeanAddition(String pContainerId, int pIndex, Map<IField<?>, Object> pNewData);

  /**
   * Registers the removal of a bean from a container within this transaction.
   *
   * @param pContainerKey the key to identify the removed bean
   */
  <KEY extends IContainerBeanKey> void registerBeanRemoval(KEY pContainerKey);

  /**
   * Registers a value change of a persistent bean within this transaction.
   *
   * @param pKey          the key to identify the changed bean
   * @param pChangedField the changed bean field
   * @param pNewValue     the new value for the field
   * @param <VALUE>       the value type of the changed field
   */
  <KEY extends IBeanKey, VALUE> void registerBeanValueChange(KEY pKey, IField<VALUE> pChangedField, VALUE pNewValue);
}
