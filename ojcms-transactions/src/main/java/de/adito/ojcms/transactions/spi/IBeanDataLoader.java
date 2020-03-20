package de.adito.ojcms.transactions.spi;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;

import java.util.*;

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
   * Loads persistent data for a bean within a container by index.
   *
   * @param pKey the key to identify the persistent bean data by index
   * @return the loaded bean data
   */
  PersistentBeanData loadContainerBeanDataByIndex(InitialIndexKey pKey);

  /**
   * Loads the type of a bean within a container at a specific index.
   * This may be necessary if the type of the container is a bean base type and the actual types are persisted in the storage system.
   *
   * @param pKey the index based key to identify the bean to load the type for
   * @return the requested bean type
   */
  <BEAN extends IBean> Class<BEAN> loadBeanTypeWithinContainer(InitialIndexKey pKey);

  /**
   * Loads persistent data for a bean within a container by identifying field values tuples.
   * This result may be empty if there's no bean for the given identifiers.
   *
   * @param pContainerId the container id the bean data is located in
   * @param pIdentifiers the field value tuples to identify the bean data
   * @return the loaded bean data or empty if no bean found
   */
  Optional<PersistentBeanData> loadContainerBeanDataByIdentifiers(String pContainerId, Map<IField<?>, Object> pIdentifiers);

  /**
   * Performs a full container load.
   *
   * @param pContainerId the id of the container to load
   * @return all persistent bean data within the container mapped by index
   */
  Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId);

  /**
   * Loads persistent data for a single bean.
   *
   * @param pKey the key to identify the persistent single bean
   * @return the loaded bean data
   */
  PersistentBeanData loadSingleBeanData(SingleBeanKey pKey);
}
