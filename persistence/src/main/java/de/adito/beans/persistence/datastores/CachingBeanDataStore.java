package de.adito.beans.persistence.datastores;

import de.adito.beans.core.IBean;
import de.adito.beans.persistence.Persist;
import de.adito.beans.persistence.spi.*;

import java.util.*;
import java.util.function.*;

/**
 * A caching persistent data store implementation for {@link IPersistentBeanDataStore}.
 * The cache maps by the persistence id, defined in {@link Persist#containerId()}.
 * The cache stores once created beans, that were injected with the persistent data cores.
 * This data store has to be provided with functions to retrieve the persistent data cores for a certain container id.
 *
 * @author Simon Danner, 18.02.2018
 */
public class CachingBeanDataStore implements IPersistentBeanDataStore
{
  private final Map<String, IPersistentBean> beanCache = new HashMap<>();
  private final Map<String, IPersistentBeanContainer> containerCache = new HashMap<>();
  private final BiFunction<String, Class<? extends IBean<?>>, IPersistentBean> beanResolver;
  private final BiFunction<String, Class<? extends IBean<?>>, IPersistentBeanContainer<?>> containerResolver;
  private final Consumer<Collection<String>> singleBeanObsoleteRemover;
  private final Consumer<Collection<String>> containerObsoleteRemover;

  /**
   * Creates the caching persistent data store.
   *
   * @param pBeanResolver              a function to get a persistent bean (data core) from a container id and a certain bean type
   * @param pContainerResolver         a function to get a persistent bean container (data core) from a container id and a certain bean type
   * @param pSingleBeanObsoleteRemover a function to clean up obsolete single beans in the persistent data store,
   *                                   takes a collection of all still existing single bean persistent ids
   * @param pContainerObsoleteRemover  a function to clean up all obsolete containers in the persistent data store,
   *                                   takes a collection of all still existing container persistent ids
   */
  public CachingBeanDataStore(BiFunction<String, Class<? extends IBean<?>>, IPersistentBean> pBeanResolver,
                              BiFunction<String, Class<? extends IBean<?>>, IPersistentBeanContainer<?>> pContainerResolver,
                              Consumer<Collection<String>> pSingleBeanObsoleteRemover,
                              Consumer<Collection<String>> pContainerObsoleteRemover)
  {
    beanResolver = Objects.requireNonNull(pBeanResolver);
    containerResolver = Objects.requireNonNull(pContainerResolver);
    singleBeanObsoleteRemover = Objects.requireNonNull(pSingleBeanObsoleteRemover);
    containerObsoleteRemover = Objects.requireNonNull(pContainerObsoleteRemover);
  }

  @Override
  public <BEAN extends IBean<BEAN>> IPersistentBean getSingleBean(String pPersistenceId, Class<BEAN> pBeanType)
  {
    return beanCache.computeIfAbsent(pPersistenceId, pId -> beanResolver.apply(pPersistenceId, pBeanType));
  }

  @Override
  public <BEAN extends IBean<BEAN>> IPersistentBeanContainer<BEAN> getContainer(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return containerCache.computeIfAbsent(pPersistenceId, pId -> containerResolver.apply(pPersistenceId, pBeanType));
  }

  @Override
  public void removeObsoleteSingleBeans(Collection<String> pStillExistingSingleBeans)
  {
    singleBeanObsoleteRemover.accept(pStillExistingSingleBeans);
  }

  @Override
  public void removeObsoleteContainers(Collection<String> pStillExistingContainerIds)
  {
    containerObsoleteRemover.accept(pStillExistingContainerIds);
  }
}
