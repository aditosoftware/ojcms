package de.adito.ojcms.transactions.spi;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;

import java.util.*;

/**
 * Defines an interface that has to been implemented by the user of this module to store bean data within a persistent storage system.
 *
 * @author Simon Danner, 26.12.2019
 */
public interface IBeanDataStorage
{
  /**
   * Registers a persistent container bean type. This may be used by the storage system to initialize required structures.
   *
   * @param pBeanType    the bean type to register
   * @param pContainerId the id of the persistent container
   */
  void registerPersistentContainerBean(Class<? extends IBean> pBeanType, String pContainerId);

  /**
   * Registers a persistent single bean type. This may be used by the storage system to initialize required structures.
   *
   * @param pBeanType the bean type to register
   * @param pBeanId   the id of the persistent single bean
   */
  void registerPersistentSingleBean(Class<? extends IBean> pBeanType, String pBeanId);

  /**
   * Processes value changes for a persistent bean within a container.
   *
   * @param pKey           the key identifying the changed bean by index
   * @param pChangedValues all changed values to process
   */
  void processChangesForContainerBean(InitialIndexKey pKey, Map<IField<?>, Object> pChangedValues);

  /**
   * Processes value changes for a persistent single bean.
   *
   * @param pKey           the key identifying the changed bean by index
   * @param pChangedValues all changed values to process
   */
  void processChangesForSingleBean(SingleBeanKey pKey, Map<IField<?>, Object> pChangedValues);

  /**
   * Processes all bean additions to a persistent container.
   *
   * @param pContainerId the id of the container
   * @param pNewData     a set of all added bean data
   */
  void processAdditionsForContainer(String pContainerId, Set<PersistentBeanData> pNewData);

  /**
   * Processes all bean removals from a persistent container.
   *
   * @param pKeysToRemoveByContainer all removed bean keys grouped by container id
   */
  void processRemovals(Map<String, Set<InitialIndexKey>> pKeysToRemoveByContainer);

  /**
   * Commits all changes to the persistent storage system within the transaction.
   */
  void commitChanges();

  /**
   * Rolls back all changes made in this transaction.
   */
  void rollbackChanges();
}
