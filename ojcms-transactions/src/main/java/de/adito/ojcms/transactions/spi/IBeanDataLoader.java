package de.adito.ojcms.transactions.spi;

import de.adito.ojcms.transactions.api.*;

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
  int loadSize(String pContainerId);

  /**
   * Loads bean data from a container by a index based key.
   *
   * @param pKey the index based key identifying the bean data
   * @return the loaded bean data
   */
  BeanData<ContainerIndexKey> loadByIndex(ContainerIndexKey pKey);

  /**
   * Loads bean data from a container by an identifier fields based key.
   *
   * @param pKey the identifier fields based key identifying the bean data
   * @return the loaded bean data
   */
  BeanData<ContainerIndexKey> loadByIdentifiers(ContainerIdentifierKey pKey);

  /**
   * Loads persistent single bean data.
   *
   * @param pKey the key of the single bean
   * @return the loaded single bean data
   */
  BeanData<String> loadSingleBean(String pKey);
}
