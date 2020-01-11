package de.adito.ojcms.transactions.spi;

import de.adito.ojcms.transactions.api.*;

import java.util.Map;

/**
 * Defines an interface that has to been implemented by the user of this module to load persistent bean data.
 *
 * @author Simon Danner, 26.12.2019
 */
public interface IBeanDataLoader
{
  /**
   * Loads the size of a persistent bean container. This method should throw an runtime exception if the container cannot be found.
   *
   * @param pContainerId the id of the container
   * @return the size of the container.
   */
  int loadContainerSize(String pContainerId);

  /**
   * Loads persistent bean data for a given key.
   *
   * @param pKey the key to identify the persistent bean data
   * @return the loaded bean data
   */
  <KEY extends IBeanKey> PersistentBeanData loadByKey(KEY pKey);

  /**
   * Performs a full container load.
   *
   * @param pContainerId the id of the container to load
   * @return all persistent bean data within the container mapped by index
   */
  Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId);
}
