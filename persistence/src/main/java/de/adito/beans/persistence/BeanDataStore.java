package de.adito.beans.persistence;

import de.adito.beans.core.*;
import de.adito.beans.persistence.spi.IPersistentBeanDataStore;

import java.util.*;

/**
 * A bean data store for persistent beans or bean containers.
 * Persistent bean elements are annotated with {@link Persist}.
 * This data store uses the persistent data store interface {@link IPersistentBeanDataStore},
 * which will be provided by the user of this persistence framework.
 * All once created beans and containers will be cached for better performance.
 *
 * @author Simon Danner, 17.02.2018
 */
public final class BeanDataStore
{
  private final IPersistentBeanDataStore dataStore;
  private final Map<String, IBean> beanCache = new HashMap<>();
  private final Map<String, IBeanContainer> containerCache = new HashMap<>();

  /**
   * Creates a new data store based on a persistent data store interface provided by the user of this persistence framework.
   *
   * @param pDataStore the persistent data store interface (may be external)
   */
  public BeanDataStore(IPersistentBeanDataStore pDataStore)
  {
    dataStore = pDataStore;
  }

  /**
   * Returns a persistent bean by its persistence ID.
   * The bean will be created, if it isn't present in the cache.
   *
   * @param pPersistenceId the persistence ID of the bean
   * @param pBeanType      the bean's type
   * @param <BEAN>         the generic bean type
   * @return the persistent bean
   */
  public <BEAN extends IBean<BEAN>> BEAN getBeanByPersistenceId(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return (BEAN) beanCache.computeIfAbsent(pPersistenceId, pId ->
        EncapsulatedBuilder.injectCustomEncapsulated(BeanPersistenceUtil.newInstance(pBeanType), dataStore.getSingleBean(pPersistenceId, pBeanType)));
  }

  /**
   * Returns a persistent bean container by its persistence ID.
   * The container will be created, if it isn't present in the cache.
   *
   * @param pPersistenceId the persistence ID of the container
   * @param pBeanType      the type of the beans in the container
   * @param <BEAN>         the generic bean type
   * @return the persistent container
   */
  public <BEAN extends IBean<BEAN>> IBeanContainer<BEAN> getContainerByPersistenceId(String pPersistenceId, Class<BEAN> pBeanType)
  {
    //noinspection unchecked
    return (IBeanContainer<BEAN>) containerCache.computeIfAbsent(pPersistenceId, pId ->
        EncapsulatedBuilder.injectCustomEncapsulated(new BeanContainer<>(pBeanType), dataStore.getContainer(pId, pBeanType)));
  }

  /**
   * Determines the persistence container id from a bean container.
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
        .orElseThrow(() -> new RuntimeException("The bean container '" + pContainer + "' is not a persistent container!"));
  }

  /**
   * Removes all obsolete persistent single beans.
   *
   * @param pStillExistingSingleBeanIds all remaining single bean ids (to find the obsoletes)
   */
  void removeObsoleteSingleBeans(Collection<String> pStillExistingSingleBeanIds)
  {
    dataStore.removeObsoleteSingleBeans(pStillExistingSingleBeanIds);
  }

  /**
   * Removes all obsolete persistent containers.
   *
   * @param pStillExistingContainerIds all remaining containers ids (to find the obsoletes)
   */
  void removeObsoleteContainers(Collection<String> pStillExistingContainerIds)
  {
    dataStore.removeObsoleteContainers(pStillExistingContainerIds);
  }
}
