package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.sql.datasource.model.PersistenceModels;
import de.adito.ojcms.sql.datasource.util.OJSQLException;
import de.adito.ojcms.sqlbuilder.OJSQLBuilder;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.*;
import java.util.*;

/**
 * Application wide {@link IBeanDataStorage} to process bean related data changes to a SQL database.
 * Also commits or rolls back changes to the transactional {@link Connection}.
 *
 * @author Simon Danner, 01.01.2020
 */
@ApplicationScoped
public class SQLBeanDataStorage implements IBeanDataStorage
{
  @Inject
  private OJSQLBuilder builder;
  @Inject
  private PersistenceModels models;
  @Inject
  private Connection connection;

  @Override
  public void registerPersistentContainerBean(Class<? extends IBean<?>> pBeanType, String pContainerId)
  {
    models.registerPersistentContainerBean(pBeanType, pContainerId);
  }

  @Override
  public void registerPersistentSingleBean(Class<? extends IBean<?>> pBeanType, String pBeanId)
  {
    models.registerPersistentSingleBean(pBeanType, pBeanId);
  }

  @Override
  public void processChangesForContainerBean(InitialIndexKey pKey, Map<IField<?>, Object> pChangedValues)
  {
    models.getContainerPersistenceModel(pKey.getContainerId()).processValueChanges(pKey.getIndex(), pChangedValues, builder);
  }

  @Override
  public void processChangesForSingleBean(SingleBeanKey pKey, Map<IField<?>, Object> pChangedValues)
  {
    models.getSingleBeanPersistenceModel(pKey.getBeanId()).processChanges(pChangedValues, builder);
  }

  @Override
  public void processAdditionsForContainer(String pContainerId, Set<PersistentBeanData> pNewBeans)
  {
    models.getContainerPersistenceModel(pContainerId).processAdditions(pNewBeans, builder);
  }

  @Override
  public void processRemovals(Map<String, Set<InitialIndexKey>> pKeysToRemoveByContainer)
  {
    pKeysToRemoveByContainer.forEach((pContainerId, pRemovedKeys) ->
                                         models.getContainerPersistenceModel(pContainerId).processRemovals(pRemovedKeys, builder));
  }

  @Override
  public void commitChanges()
  {
    try
    {
      connection.commit();
    }
    catch (SQLException pE)
    {
      throw new OJSQLException("Commit failed!", pE);
    }
  }

  @Override
  public void rollbackChanges()
  {
    try
    {
      connection.rollback();
    }
    catch (SQLException pE)
    {
      throw new OJSQLException("Rollback failed!", pE);
    }
  }
}
