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
   * Registers a persistent bean type. This may be used by the storage system to initialize required structures.
   *
   * @param pBeanType    the bean type to register with the key
   * @param pContainerId the id of the persistent container
   * @param pIsContainer <tt>true</tt> if the persistent beans should be stored in a container
   */
  void registerPersistentBean(Class<? extends IBean<?>> pBeanType, String pContainerId, boolean pIsContainer);

  /**
   * Processes value changes for a persistent bean.
   *
   * @param pKey           the key identifying the changed bean
   * @param pChangedValues all changed values to process
   */
  <KEY extends IBeanKey> void processChangesForBean(KEY pKey, Map<IField<?>, Object> pChangedValues);

  /**
   * Processes all bean additions to a persistent container.
   *
   * @param pContainerId the id of the container
   * @param pNewData     a list of all added bean data
   */
  void processAdditionsForContainer(String pContainerId, List<PersistentBeanData> pNewData);

  /**
   * Processes all bean removals from a persistent container.
   *
   * @param pKeysToRemove a set of container bean keys
   */
  void processRemovals(Set<IContainerBeanKey> pKeysToRemove);

  /**
   * Commits all changes to the persistent storage system within the transaction.
   */
  void commitChanges();

  /**
   * Rolls back all changes made in this transaction.
   */
  void rollbackChanges();
}
