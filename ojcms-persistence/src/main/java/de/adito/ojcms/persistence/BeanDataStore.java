package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.datasource.IBeanContainerDataSource;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import de.adito.ojcms.persistence.datastores.IPersistentSourcesStore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A bean data store for persistent beans or bean containers.
 * Persistent bean types are annotated with {@link Persist}.
 * The store uses a {@link IPersistentSourcesStore} to retrieve data sources for certain {@link Persist#containerId()}.
 * This class is mainly responsible for creating real bean instances with the persistent data sources from the store mentioned above.
 * All once created beans and containers will be cached for better performance.
 *
 * @author Simon Danner, 17.02.2018
 */
public final class BeanDataStore
{
  private final IPersistentSourcesStore dataSources;
  private final Map<String, IBean<?>> beanCache = new ConcurrentHashMap<>();
  private final Map<String, IBeanContainer<?>> containerCache = new ConcurrentHashMap<>();

  /**
   * Creates a new data store based on a {@link IPersistentSourcesStore} provided by the user of this persistence framework.
   *
   * @param pDataSources a store for persistent data sources for beans and containers
   */
  BeanDataStore(IPersistentSourcesStore pDataSources)
  {
    dataSources = Objects.requireNonNull(pDataSources);
  }

  /**
   * Returns a persistent bean by its persistence id.
   * The bean will be created, if it isn't present in the cache.
   *
   * @param pPersistenceId the persistence id of the bean
   * @param pBeanType      the bean's type
   * @param <BEAN>         the generic bean type
   * @return the persistent bean
   */
  @SuppressWarnings("unchecked")
  public <BEAN extends IBean<BEAN>> BEAN getBeanByPersistenceId(String pPersistenceId, Class<BEAN> pBeanType)
  {
    return (BEAN) beanCache.computeIfAbsent(pPersistenceId, pId -> {
      final BEAN bean = newPersistentBeanInstance(pBeanType);
      Set<FieldValueTuple<?>> initializedTuples = null;
      //If the bean wasn't existing before, store tuples that where initialized by the default constructor
      if (!dataSources.isSingleBeanSourceExisting(pPersistenceId))
        initializedTuples = bean.stream()
            .filter(pTuple -> !pTuple.isInitialValue())
            .collect(Collectors.toSet());
      bean.setEncapsulatedDataSource(dataSources.getSingleBeanDataSource(pPersistenceId, pBeanType));
      if (initializedTuples != null)
        initializedTuples.forEach(pTuple -> bean.setValue((IField) pTuple.getField(), pTuple.getValue()));
      return bean;
    });
  }

  /**
   * Returns a persistent bean container by its persistence id.
   * The container will be created, if it isn't present in the cache.
   *
   * @param pPersistenceId the persistence id of the container
   * @param pBeanType      the type of the beans in the container
   * @param <BEAN>         the generic bean type
   * @return the persistent container
   */
  public <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> getContainerByPersistenceId(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return (IBeanContainer<BEAN>) containerCache.computeIfAbsent(pPersistenceId, pId -> {
      final IBeanContainer<BEAN> container = IBeanContainer.empty(pBeanType);
      final IBeanContainerDataSource<BEAN> containerDataSource = dataSources.getContainerDataSource(pId, pBeanType);
      container.setEncapsulatedDataSource(containerDataSource);
      return container;
    });
  }

  /**
   * Determines the persistence container id of a bean container.
   * The container has to be created automatically by this framework to be found.
   *
   * @param pContainer the container to search the container id for
   * @return the container id
   */
  public String findContainerId(IBeanContainer<?> pContainer)
  {
    return containerCache.entrySet().stream()
        .filter(pEntry -> pEntry.getValue() == pContainer)
        .findAny()
        .map(Map.Entry::getKey)
        .orElseThrow(() -> new RuntimeException("The container '" + pContainer + "' is not registered within the persistence framework!"));
  }

  /**
   * Removes all obsolete persistent single bean data sources.
   *
   * @param pStillExistingSingleBeanIds all remaining single bean ids (to find the obsoletes)
   */
  void removeObsoleteSingleBeans(Collection<String> pStillExistingSingleBeanIds)
  {
    dataSources.removeObsoleteSingleBeans(pStillExistingSingleBeanIds);
  }

  /**
   * Removes all obsolete persistent container data sources.
   *
   * @param pStillExistingContainerIds all remaining container ids (to find the obsoletes)
   */
  void removeObsoleteContainers(Collection<String> pStillExistingContainerIds)
  {
    dataSources.removeObsoleteContainers(pStillExistingContainerIds);
  }

  /**
   * Instantiates a new bean from a certain bean type.
   * The bean type has to annotated with {@link Persist}, otherwise a runtime exception will be thrown.
   *
   * @param pBeanType the bean type to instantiate
   * @param <BEAN>    the generic bean type
   * @return the create bean instance.
   * @throws RuntimeException if the persistence annotation is missing
   */
  public static <BEAN extends IBean<BEAN>> BEAN newPersistentBeanInstance(Class<BEAN> pBeanType)
  {
    try
    {
      if (!pBeanType.isAnnotationPresent(Persist.class))
        throw new RuntimeException("The bean type '" + pBeanType.getName() + "' is not marked as persistent bean!");
      return pBeanType.newInstance();
    }
    catch (InstantiationException | IllegalAccessException pE)
    {
      throw new RuntimeException("The persistent bean type '" + pBeanType.getName() + "' must define a default constructor!");
    }
  }
}
