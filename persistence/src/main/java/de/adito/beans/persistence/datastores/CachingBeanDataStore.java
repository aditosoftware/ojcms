package de.adito.beans.persistence.datastores;

import de.adito.beans.core.IBean;
import de.adito.beans.persistence.Persist;
import de.adito.beans.persistence.spi.*;

import java.util.*;
import java.util.function.*;

/**
 * A caching persistent data store implementation for {@link IPersistentBeanDataStore}.
 * The cache maps by the persistence id defined in {@link Persist#containerId()}.
 * The cache stores once created beans, that were injected with the persistent data cores.
 * This data store has to be provided with functions to retrieve the persistent data cores for a certain container id.
 *
 * @author Simon Danner, 18.02.2018
 */
class CachingBeanDataStore implements IPersistentBeanDataStore
{
  private final Map<String, IPersistentBean> beanCache = new HashMap<>();
  private final Map<String, IPersistentBeanContainer> containerCache = new HashMap<>();
  private final Function<String, IPersistentBean> beanResolver;
  private final BiFunction<String, Class<? extends IBean<?>>, IPersistentBeanContainer> containerResolver;

  /**
   * Create the caching persistent data store.
   *
   * @param pBeanResolver      a function to get a persistent bean (data core) from a container id
   * @param pContainerResolver a function to get a persistent bean container (data core) from a container id and a certain bean type
   */
  public CachingBeanDataStore(Function<String, IPersistentBean> pBeanResolver,
                              BiFunction<String, Class<? extends IBean<?>>, IPersistentBeanContainer> pContainerResolver)
  {
    beanResolver = pBeanResolver;
    containerResolver = pContainerResolver;
  }

  @Override
  public IPersistentBean getSingleBean(String pPersistenceId)
  {
    return beanCache.computeIfAbsent(pPersistenceId, pId -> beanResolver.apply(pPersistenceId));
  }

  @Override
  public <BEAN extends IBean<BEAN>> IPersistentBeanContainer<BEAN> getContainer(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return containerCache.computeIfAbsent(pPersistenceId, pId -> containerResolver.apply(pPersistenceId, pBeanType));
  }
}
