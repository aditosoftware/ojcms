package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.sql.datasource.model.*;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Connection;
import java.util.Map;

/**
 * Application wide {@link IBeanDataLoader} to load bean related data from a SQL database.
 * No transactional caching is performed here. This will be done by the transaction itself.
 * This only acts as reader for the storage system (in this case the SQL database).
 * The {@link Connection} on the other side is {@link TransactionalScoped}.
 * So changes made by other transaction won't influence the results read by this class.
 *
 * @author Simon Danner, 01.01.2020
 */
@ApplicationScoped
public class SQLBeanDataLoader implements IBeanDataLoader
{
  @Inject
  private OJSQLBuilder builder;
  @Inject
  private PersistenceModels models;

  @Override
  public int loadContainerSize(String pContainerId)
  {
    return _requiresContainerModel(pContainerId).loadSize(builder);
  }

  @Override
  public <KEY extends IBeanKey> PersistentBeanData loadByKey(KEY pKey)
  {
    return models.getPersistenceModel(pKey.getContainerId()).loadDataByKey(pKey, builder);
  }

  @Override
  public Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId)
  {
    return _requiresContainerModel(pContainerId).loadFullData(builder);
  }

  /**
   * Tries to resolves a {@link ContainerPersistenceModel} by container id.
   * Throws a {@link OJDatabaseException} if the model behind the id does not relate to a bean container.
   *
   * @param pContainerId the id of the container to resolve the model for
   * @return the container model for the requested id
   */
  private ContainerPersistenceModel _requiresContainerModel(String pContainerId)
  {
    final IPersistenceModel<?> model = models.getPersistenceModel(pContainerId);

    if (!(model instanceof ContainerPersistenceModel))
      throw new OJDatabaseException("No bean container model found for container id: " + pContainerId);

    return (ContainerPersistenceModel) model;
  }
}
