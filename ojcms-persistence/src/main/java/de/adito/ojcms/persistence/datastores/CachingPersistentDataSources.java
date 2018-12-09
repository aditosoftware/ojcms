package de.adito.ojcms.persistence.datastores;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * A caching persistent data store implementation for {@link IPersistentSourcesStore}.
 * The cache maps by the persistence id, defined in {@link de.adito.ojcms.persistence.Persist#containerId()}.
 * The cache stores once created beans, that were injected with the persistent data cores.
 * This data store has to be provided with functions to retrieve the persistent data cores for a certain container id.
 *
 * @author Simon Danner, 18.02.2018
 */
final class CachingPersistentDataSources implements IPersistentSourcesStore
{
  private final Map<String, IBeanDataSource> beanDataSourcesCache = new ConcurrentHashMap<>();
  private final Map<String, IBeanContainerDataSource<?>> containerDataSourcesCache = new ConcurrentHashMap<>();
  private final BiFunction<String, Class<? extends IBean<?>>, IBeanDataSource> beanResolver;
  private final Function<String, Boolean> beanExistingDeterminer;
  private final BiFunction<String, Class<? extends IBean<?>>, IBeanContainerDataSource<?>> containerResolver;
  private final Consumer<Collection<String>> singleBeanObsoleteRemover;
  private final Consumer<Collection<String>> containerObsoleteRemover;

  /**
   * Creates the caching persistent data store.
   *
   * @param pBeanResolver              a function to get a persistent bean (data core) from a container id and a certain bean type
   * @param pBeanExistingDeterminer    a function that determines if a single bean data source is existing by its persistence id
   * @param pContainerResolver         a function to get a persistent bean container (data core) from a container id and a certain bean type
   * @param pSingleBeanObsoleteRemover a function to clean up obsolete single beans in the persistent data store,
   *                                   takes a collection of all still existing single bean persistent ids
   * @param pContainerObsoleteRemover  a function to clean up all obsolete containers in the persistent data store,
   */
  CachingPersistentDataSources(BiFunction<String, Class<? extends IBean<?>>, IBeanDataSource> pBeanResolver,
                               Function<String, Boolean> pBeanExistingDeterminer,
                               BiFunction<String, Class<? extends IBean<?>>, IBeanContainerDataSource<?>> pContainerResolver,
                               Consumer<Collection<String>> pSingleBeanObsoleteRemover,
                               Consumer<Collection<String>> pContainerObsoleteRemover)
  {
    beanResolver = Objects.requireNonNull(pBeanResolver);
    beanExistingDeterminer = pBeanExistingDeterminer;
    containerResolver = Objects.requireNonNull(pContainerResolver);
    singleBeanObsoleteRemover = Objects.requireNonNull(pSingleBeanObsoleteRemover);
    containerObsoleteRemover = Objects.requireNonNull(pContainerObsoleteRemover);
  }

  @Override
  public <BEAN extends IBean<BEAN>> IBeanDataSource getSingleBeanDataSource(String pPersistenceId, Class<BEAN> pBeanType)
  {
    return beanDataSourcesCache.computeIfAbsent(pPersistenceId, pId -> beanResolver.apply(pPersistenceId, pBeanType));
  }

  @Override
  public boolean isSingleBeanSourceExisting(String pPersistenceId)
  {
    return beanExistingDeterminer.apply(pPersistenceId);
  }

  @Override
  public <BEAN extends IBean<BEAN>> IBeanContainerDataSource<BEAN> getContainerDataSource(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return (IBeanContainerDataSource<BEAN>)
        containerDataSourcesCache.computeIfAbsent(pPersistenceId, pId -> containerResolver.apply(pPersistenceId, pBeanType));
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
