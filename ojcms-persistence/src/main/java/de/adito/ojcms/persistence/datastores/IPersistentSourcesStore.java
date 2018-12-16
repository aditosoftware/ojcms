package de.adito.ojcms.persistence.datastores;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.persistence.Persist;

import java.util.Collection;
import java.util.function.Function;

/**
 * A store for persistent bean data sources.
 * A data store can provide single bean or bean container data sources. These are identified by a persistence id.
 * This container ids are defined via {@link Persist#containerId()}.
 * This is the entry point for the automatic detection and preparation of the beans/containers of this framework.
 * The user has to provide a certain data store for the framework via {@link de.adito.ojcms.persistence.OJPersistence#configure(Function)}
 *
 * The bean or container data sources that can be obtained by their persistence ids, aren't complete bean elements.
 * They just define the minimal data sources to create the data cores later on.
 * This enables another level of abstraction, because the data store just has to define the general and least necessary functionality.
 *
 * @author Simon Danner, 14.02.2018
 */
public interface IPersistentSourcesStore
{
  /**
   * A persistent bean data source for a certain persistence id.
   *
   * @param pPersistenceId the persistence id
   * @param pBeanType      the type of the single bean the source is for
   * @return the persistent bean data source
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
   * A persistent bean container data source for a certain persistence id.
   *
   * @param pPersistenceId the persistence id
   * @param pBeanType      the type of the beans in the container
   * @param <BEAN>         the generic bean type the source is for
   * @return the persistent container data source
   */
  <BEAN extends IBean<BEAN>> IBeanContainerDataSource<BEAN> getContainerDataSource(String pPersistenceId, Class<BEAN> pBeanType);

  /**
   * Removes all obsolete persistent single bean data sources from this store.
   *
   * @param pStillExistingSingleBeans all remaining single beans (to find the obsoletes)
   */
  void removeObsoleteSingleBeans(Collection<IBean<?>> pStillExistingSingleBeans);

  /**
   * Removes all obsolete persistent bean container data sources from this data store.
   *
   * @param pStillExistingContainerIds all remaining container ids (to find the obsoletes)
   */
  void removeObsoleteContainers(Collection<String> pStillExistingContainerIds);
}
