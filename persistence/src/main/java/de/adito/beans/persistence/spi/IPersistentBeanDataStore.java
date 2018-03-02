package de.adito.beans.persistence.spi;

import de.adito.beans.core.IBean;
import de.adito.beans.persistence.Persist;

import java.util.function.Function;

/**
 * An interface for a persistence data store for bean elements.
 * A data store can provide single beans or bean containers. These are identified by an container id.
 * The container id will be defined in {@link Persist#containerId()}.
 * This is the entry point for the automatic detection and preparation of the beans/containers of this framework.
 * The user has to provide a certain data store for the framework via {@link de.adito.beans.persistence.OJPersistence#configure(Function)}
 *
 * The beans or containers, which can be obtained by their persistence ids, aren't complete bean elements.
 * They just define, how the encapsulated data core can be created. (see {@link de.adito.beans.core.EncapsulatedBuilder})
 * This enables another level of abstraction, because the data store just has to define the general and least necessary functionality.
 *
 * @author Simon Danner, 14.02.2018
 */
public interface IPersistentBeanDataStore
{
  /**
   * A persistent (single) bean (builder for the encapsulated data core) for a certain persistence id.
   *
   * @param pPersistenceId the persistence ID
   * @return the persistent bean
   */
  IPersistentBean getSingleBean(String pPersistenceId);

  /**
   * A persistent bean container (builder for the encapsulated data core) for a certain persistence id.
   *
   * @param pPersistenceId the persistence ID
   * @param pBeanType      the type of the beans in the container
   * @param <BEAN>         the generic bean type
   * @return the persistent container
   */
  <BEAN extends IBean<BEAN>> IPersistentBeanContainer<BEAN> getContainer(String pPersistenceId, Class<BEAN> pBeanType);
}
