package de.adito.ojcms.transactions.spi;

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
   * Processes value changes for a bean within a container.
   *
   * @param pKey           the index based key identified the bean
   * @param pChangedValues all changed values to process
   */
  void processChangesForBean(ContainerIndexKey pKey, Map<IField<?>, Object> pChangedValues);

  /**
   * Processes value changes for single persistent bean.
   *
   * @param pKey           the id of the single bean
   * @param pChangedValues all changed values to process
   */
  void processChangesForSingleBean(String pKey, Map<IField<?>, Object> pChangedValues);

  /**
   * Processes all bean additions to a persistent container.
   *
   * @param pContainerId the id of the container
   * @param pNewData     a list of all added bean data
   */
  void processAdditionsForContainer(String pContainerId, List<BeanData<ContainerIndexKey>> pNewData);

  /**
   * Processes all bean removals from a persistent container by index.
   *
   * @param pKeysToRemove a set of index based keys
   */
  void processRemovalsById(Set<ContainerIndexKey> pKeysToRemove);

  /**
   * Processes all bean removals from a persistent container by identifying fields.
   *
   * @param pKeysToRemove a set of identifier fields based keys
   */
  void processRemovalsByIdentifiers(Set<ContainerIdentifierKey> pKeysToRemove);
}
