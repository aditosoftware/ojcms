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
   * Requests persistent data of a bean within a container. The bean is identified by its index for this method.
   * The exception handling (in case a bean cannot be found within the container etc.) must be defined by the classes implementing the SPI.
   *
   * @param pIndexBasedKey the index based bean key
   * @return the requested bean data
   */
  BeanData<ContainerIndexKey> requestBeanDataFromContainer(ContainerIndexKey pIndexBasedKey);

  /**
   * Requests persistent data of a bean within a container. The bean is identified by its identifying fields.
   * The exception handling (in case a bean cannot be found within the container etc.) must be defined by the classes implementing the SPI.
   *
   * @param pIdentifierKey the key based on fields marked as identifier
   * @return the requested bean data
   */
  BeanData<ContainerIndexKey> requestBeanDataFromContainer(ContainerIdentifierKey pIdentifierKey);

  /**
   * Requests persistent single bean data by its string based id.
   * The exception handling (in case the single bean is not existing etc.) must be defined by the classes implementing the SPI.
   *
   * @param pSingleBeanId the id of the single bean
   * @return the requested single bean data
   */
  BeanData<String> requestSingleBeanData(String pSingleBeanId);

  /**
   * Registers the addition of a bean in a container within this transaction.
   *
   * @param pContainerId the id of the container the bean has been added to
   * @param pIndex       the index of the added bean
   * @param pNewData     the data of the added bean
   */
  void registerBeanAddition(String pContainerId, int pIndex, Map<IField<?>, Object> pNewData);

  /**
   * Registers the removal of a bean from a container within this transaction.
   *
   * @param pIndexBasedKey the index based key that identifies the removed bean
   */
  void registerBeanRemoval(ContainerIndexKey pIndexBasedKey);

  /**
   * Registers the removal of a bean from a container within this transaction.
   *
   * @param pIdentifierKey the identifier fields based key that identifies the removed bean
   */
  void registerBeanRemoval(ContainerIdentifierKey pIdentifierKey);

  /**
   * Registers a value change of a persistent bean within a container within this transaction.
   *
   * @param pContainerKey the id of the container that changed bean is located in
   * @param pChangedField the changed bean field
   * @param pNewValue     the new value for the field
   * @param <VALUE>       the value type of the changed field
   */
  <VALUE> void registerBeanValueChange(ContainerIndexKey pContainerKey, IField<VALUE> pChangedField, VALUE pNewValue);

  /**
   * Registers a value change of single persistent bean within this transaction.
   *
   * @param pSingleBeanId the id of the single bean
   * @param pChangedField the changed bean field
   * @param pNewValue     the new value for the field
   * @param <VALUE>       the value type of the changed field
   */
  <VALUE> void registerSingleBeanValueChange(String pSingleBeanId, IField<VALUE> pChangedField, VALUE pNewValue);
}
