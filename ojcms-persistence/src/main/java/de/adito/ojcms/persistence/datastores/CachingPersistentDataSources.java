package de.adito.ojcms.persistence.datastores;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * A caching implementation for {@link IPersistentSourcesStore}.
 * The cache maps by the persistence id, defined in {@link de.adito.ojcms.persistence.Persist#containerId()}.
 * The cache stores once created data sources.
 * This data sources store has to be provided with functions to retrieve the persistent sources for a certain container id.
 *
 * @author Simon Danner, 18.02.2018
 */
final class CachingPersistentDataSources implements IPersistentSourcesStore
{
  private final Map<String, IBeanDataSource> beanDataSourcesCache = new ConcurrentHashMap<>();
  private final Map<String, IBeanContainerDataSource<?>> containerDataSourcesCache = new ConcurrentHashMap<>();
  private final BiFunction<String, Class<? extends IBean<?>>, IBeanDataSource> beanResolver;
  private final Predicate<String> beanExistingDeterminer;
  private final BiFunction<String, Class<? extends IBean<?>>, IBeanContainerDataSource<?>> containerResolver;
  private final Consumer<Collection<IBean<?>>> singleBeanObsoleteRemover;
  private final Consumer<Collection<String>> containerObsoleteRemover;

  /**
   * Creates the caching persistent data sources store.
   *
   * @param pBeanResolver              a function to get a persistent bean data source from a container id and a certain bean type
   * @param pBeanExistingDeterminer    a predicate that determines if a single bean data source is existing by its persistence id
   * @param pContainerResolver         a function to get a persistent bean container data source from a container id and a certain bean type
   * @param pSingleBeanObsoleteRemover a function to clean up obsolete single bean sources in the persistent data store,
   *                                   takes a collection of all still existing single beans
   * @param pContainerObsoleteRemover  a function to clean up all obsolete container data sources in the persistent data store,
   *                                   takes a collection of all still existing container persistent ids
   */
  CachingPersistentDataSources(BiFunction<String, Class<? extends IBean<?>>, IBeanDataSource> pBeanResolver,
                               Predicate<String> pBeanExistingDeterminer,
                               BiFunction<String, Class<? extends IBean<?>>, IBeanContainerDataSource<?>> pContainerResolver,
                               Consumer<Collection<IBean<?>>> pSingleBeanObsoleteRemover,
                               Consumer<Collection<String>> pContainerObsoleteRemover)
  {
    beanResolver = Objects.requireNonNull(pBeanResolver);
    beanExistingDeterminer = Objects.requireNonNull(pBeanExistingDeterminer);
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
    return beanExistingDeterminer.test(pPersistenceId);
  }

  @Override
  public <BEAN extends IBean<BEAN>> IBeanContainerDataSource<BEAN> getContainerDataSource(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return (IBeanContainerDataSource<BEAN>)
        containerDataSourcesCache.computeIfAbsent(pPersistenceId, pId -> containerResolver.apply(pPersistenceId, pBeanType));
  }

  @Override
  public void removeObsoleteSingleBeans(Collection<IBean<?>> pStillExistingSingleBeans)
  {
    singleBeanObsoleteRemover.accept(pStillExistingSingleBeans);
  }

  @Override
  public void removeObsoleteContainers(Collection<String> pStillExistingContainerIds)
  {
    containerObsoleteRemover.accept(pStillExistingContainerIds);
  }
}
