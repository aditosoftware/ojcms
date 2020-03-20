package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all {@link IPersistenceModel} of the application.
 *
 * @author Simon Danner, 01.01.2020
 */
@ApplicationScoped
public class PersistenceModels
{
  private final Map<String, ContainerPersistenceModel> containerModels = new ConcurrentHashMap<>();
  private final Map<String, SingleBeanPersistenceModel> singleBeanModels = new ConcurrentHashMap<>();

  /**
   * Registers a persistent container bean type to create its {@link ContainerPersistenceModel}.
   *
   * @param pBeanType    the bean type of the persistent bean
   * @param pContainerId the id of the persistent container
   */
  public void registerPersistentContainerBean(Class<? extends IBean> pBeanType, String pContainerId)
  {
    containerModels.putIfAbsent(pContainerId, new ContainerPersistenceModel(pContainerId, pBeanType));
  }

  /**
   * Registers a persistent base container to create its {@link ContainerPersistenceModel}.
   *
   * @param pSubTypes    all supported sub bean types of the container
   * @param pContainerId the id of the persistent container
   */
  public void registerPersistentBaseContainer(Set<Class<? extends IBean>> pSubTypes, String pContainerId)
  {
    containerModels.putIfAbsent(pContainerId, new BaseContainerPersistenceModel(pContainerId, pSubTypes));
  }

  /**
   * Registers a persistent single bean type to create its {@link SingleBeanPersistenceModel}.
   *
   * @param pBeanType the bean type of the persistent bean
   * @param pBeanId   the id of the persistent single bean
   */
  public void registerPersistentSingleBean(Class<? extends IBean> pBeanType, String pBeanId)
  {
    singleBeanModels.putIfAbsent(pBeanId, new SingleBeanPersistenceModel(pBeanId, pBeanType));
  }

  /**
   * Resolves a {@link ContainerPersistenceModel} by the id of the persistent container.
   *
   * @param pContainerId the id of the persistent container
   * @return the resolved container persistence model
   */
  public ContainerPersistenceModel getContainerPersistenceModel(String pContainerId)
  {
    if (!containerModels.containsKey(pContainerId))
      throw new IllegalArgumentException("No container bean model found for container id: " + pContainerId);

    return containerModels.get(pContainerId);
  }

  /**
   * Resolves a {@link BaseContainerPersistenceModel} by the id of the persistent container.
   * Throws a {@link ClassCastException} if the requested model is not for a base container.
   *
   * @param pContainerId the id of the persistent container
   * @return the resolved container persistence model
   */
  public BaseContainerPersistenceModel getBaseContainerPersistenceModel(String pContainerId)
  {
    return (BaseContainerPersistenceModel) getContainerPersistenceModel(pContainerId);
  }

  /**
   * Resolves a {@link SingleBeanPersistenceModel} by the id of the persistent single bean.
   *
   * @param pBeanId the id of the persistent single bean
   * @return the resolved single bean persistence model
   */
  public SingleBeanPersistenceModel getSingleBeanPersistenceModel(String pBeanId)
  {
    if (!singleBeanModels.containsKey(pBeanId))
      throw new IllegalArgumentException("No single bean model found for bean id: " + pBeanId);

    return singleBeanModels.get(pBeanId);
  }

  /**
   * Initializes all registered {@link IPersistenceModel} instances in the database.
   *
   * @param pBuilder a builder to execute SQL statement.
   */
  public void initAllModels(OJSQLBuilder pBuilder)
  {
    containerModels.values().forEach(pModel -> pModel.initModelInDatabase(pBuilder));
    singleBeanModels.values().forEach(pModel -> pModel.initModelInDatabase(pBuilder));
  }

  /**
   * Resolves all database table names of registered {@link ContainerPersistenceModel} instances.
   *
   * @return a list of table names
   */
  public List<String> getAllContainerTableNames()
  {
    return new ArrayList<>(containerModels.keySet());
  }

  /**
   * Resolves all database row ids of registered {@link SingleBeanPersistenceModel} instances.
   *
   * @return a list of row ids
   */
  public List<String> getAllSingleBeanIds()
  {
    return new ArrayList<>(singleBeanModels.keySet());
  }
}
