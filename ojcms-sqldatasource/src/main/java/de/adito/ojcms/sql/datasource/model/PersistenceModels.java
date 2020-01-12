package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.transactions.api.IBeanKey;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Manages all {@link IPersistenceModel} of the application.
 *
 * @author Simon Danner, 01.01.2020
 */
@ApplicationScoped
public class PersistenceModels
{
  private final Map<String, IPersistenceModel<?>> models = new ConcurrentHashMap<>();

  /**
   * Registers a persistent bean type to create its suitable {@link IPersistenceModel}.
   *
   * @param pBeanType    the bean type of the persistent beans
   * @param pContainerId the id of the persistent container
   * @param pIsContainer <tt>true</tt> if the beans for the given bean type should be stored in a container
   */
  public void registerPersistentBean(Class<? extends IBean<?>> pBeanType, String pContainerId, boolean pIsContainer)
  {
    final IPersistenceModel<? extends IBeanKey> model = pIsContainer ? new ContainerPersistenceModel(pContainerId, pBeanType) :
        new SingleBeanPersistenceModel(pContainerId, pBeanType);
    models.put(pContainerId, model);
  }

  /**
   * Resolves a {@link IPersistenceModel} by the id of the persistent container.
   *
   * @param pContainerId the id of the persistent container
   * @param <KEY>        the type of the bean key to identify bean data within the persistence model
   * @return the resolved persistence model
   */
  public <KEY extends IBeanKey> IPersistenceModel<KEY> getPersistenceModel(String pContainerId)
  {
    if (!models.containsKey(pContainerId))
      throw new IllegalArgumentException("No bean model found for container id: " + pContainerId);

    //noinspection unchecked
    return (IPersistenceModel<KEY>) models.get(pContainerId);
  }

  /**
   * Initializes all registered {@link IPersistenceModel} instances in the database.
   *
   * @param pBuilder a builder to execute SQL statement.
   */
  public void initAllModels(OJSQLBuilder pBuilder)
  {
    models.values().forEach(pModel -> pModel.initModelInDatabase(pBuilder));
  }

  /**
   * Resolves all database table names of registered {@link ContainerPersistenceModel} instances.
   *
   * @return a list of table names
   */
  public List<String> getAllContainerTableNames()
  {
    return _getContainerIdsByPredicate(pModel -> pModel instanceof ContainerPersistenceModel);
  }

  /**
   * Resolves all database row ids of registered {@link SingleBeanPersistenceModel} instances.
   *
   * @return a list of row ids
   */
  public List<String> getAllSingleBeanIds()
  {
    return _getContainerIdsByPredicate(pModel -> pModel instanceof SingleBeanPersistenceModel);
  }

  /**
   * Resolves persistent container ids by a {@link Predicate} based on a {@link IPersistenceModel}.
   *
   * @param pPredicate the predicate to filter the requested persistence models
   * @return a list of container ids of filtered models
   */
  private List<String> _getContainerIdsByPredicate(Predicate<IPersistenceModel<?>> pPredicate)
  {
    return models.entrySet().stream()
        .filter(pEntry -> pPredicate.test(pEntry.getValue()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }
}
