package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.sql.datasource.model.PersistenceModels;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Connection;
import java.util.*;

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
    return models.getContainerPersistenceModel(pContainerId).loadSize(builder);
  }

  @Override
  public PersistentBeanData loadContainerBeanDataByIndex(InitialIndexKey pKey)
  {
    return models.getContainerPersistenceModel(pKey.getContainerId()).loadDataByIndex(pKey, builder);
  }

  @Override
  public <BEAN extends IBean> Class<BEAN> loadBeanTypeWithinContainer(InitialIndexKey pKey)
  {
    return models.getBaseContainerPersistenceModel(pKey.getContainerId()).loadBeanType(pKey, builder);
  }

  @Override
  public Optional<PersistentBeanData> loadContainerBeanDataByIdentifiers(String pContainerId, Map<IField<?>, Object> pIdentifiers)
  {
    return models.getContainerPersistenceModel(pContainerId).loadDataByIdentifiers(pIdentifiers, builder);
  }

  @Override
  public PersistentBeanData loadSingleBeanData(SingleBeanKey pKey)
  {
    return models.getSingleBeanPersistenceModel(pKey.getBeanId()).loadSingleBeanData(pKey, builder);
  }

  @Override
  public Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId)
  {
    return models.getContainerPersistenceModel(pContainerId).loadFullData(builder);
  }
}
