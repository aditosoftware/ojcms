package de.adito.ojcms.persistence.datastores;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.persistence.Persist;

import java.util.Collection;
import java.util.function.Function;

/**
 * An interface for a persistence data store for bean elements.
 * A data store can provide single bean or bean container data sources. These are identified by a persistence id.
 * This container id will be defined by {@link Persist#containerId()}.
 * This is the entry point for the automatic detection and preparation of the beans/containers of this framework.
 * The user has to provide a certain data store for the framework via {@link de.adito.ojcms.persistence.OJPersistence#configure(Function)}
 *
 * The beans or containers, which can be obtained by their persistence ids, aren't complete bean elements.
 * They just define the minimal data sources to create the elements from.
 * This enables another level of abstraction, because the data store just has to define the general and least necessary functionality.
 *
 * @author Simon Danner, 14.02.2018
 */
public interface IPersistentSourcesStore
{
  /**
   * A persistent (single) bean (builder for the encapsulated data core) for a certain persistence id.
   *
   * @param pPersistenceId the persistence id
   * @param pBeanType      the type of the single bean
   * @return the persistent bean
   */
  <BEAN extends IBean<BEAN>> IBeanDataSource getSingleBeanDataSource(String pPersistenceId, Class<BEAN> pBeanType);

  /**
   * Determines, if a data source for a certain single bean id is present in the store.
   *
   * @param pPersistenceId the persistence id of the single bean
   * @return <tt>true</tt> if the data source is existing
   */
  boolean isSingleBeanSourceExisting(String pPersistenceId);

  /**
   * A persistent bean container (builder for the encapsulated data core) for a certain persistence id.
   *
   * @param pPersistenceId the persistence id
   * @param pBeanType      the type of the beans in the container
   * @param <BEAN>         the generic bean type
   * @return the persistent container
   */
  <BEAN extends IBean<BEAN>> IBeanContainerDataSource<BEAN> getContainerDataSource(String pPersistenceId, Class<BEAN> pBeanType);

  /**
   * Removes all obsolete persistent single beans from this data store.
   *
   * @param pStillExistingSingleBeanIds all remaining single bean ids (to find the obsoletes)
   */
  void removeObsoleteSingleBeans(Collection<String> pStillExistingSingleBeanIds);

  /**
   * Removes all obsolete persistent bean containers from this data store.
   *
   * @param pStillExistingContainerIds all remaining container ids (to find the obsoletes)
   */
  void removeObsoleteContainers(Collection<String> pStillExistingContainerIds);
}
