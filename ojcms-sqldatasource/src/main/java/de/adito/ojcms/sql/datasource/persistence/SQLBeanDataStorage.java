package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.sql.datasource.model.*;
import de.adito.ojcms.sql.datasource.model.column.*;
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
  public void registerPersistentBean(Class<? extends IBean<?>> pBeanType, String pContainerId, boolean pIsContainer)
  {
    models.registerPersistentBean(pBeanType, pContainerId, pIsContainer);
  }

  @Override
  public <KEY extends IBeanKey> void processChangesForBean(KEY pKey, Map<IField<?>, Object> pChangedValues)
  {
    if (pKey instanceof BeanIndexKey)
      _updateById((BeanIndexKey) pKey, pChangedValues);
    else if (pKey instanceof BeanIdentifiersKey)
      _updateByIdentifiers((BeanIdentifiersKey) pKey, pChangedValues);
    else if (pKey instanceof SingleBeanKey)
      _updateSingleBean(pKey.getContainerId(), pChangedValues);
    else
      throw new UnsupportedOperationException("Key of type " + pKey.getClass().getName() + " not supported for update!");
  }

  @Override
  public void processAdditionsForContainer(String pContainerId, List<PersistentBeanData> pNewData)
  {
    pNewData.forEach(pNewEntry -> builder.doInsert(pInsert -> pInsert
        .into(pContainerId)
        .atIndex(pNewEntry.getIndex())
        .values(BeanColumnValueTuple.ofMap(pNewEntry.getData()))
        .insert()));
  }

  @Override
  public void processRemovals(Set<IContainerBeanKey> pKeysToRemove)
  {
    for (IContainerBeanKey key : pKeysToRemove)
    {
      if (key instanceof BeanIndexKey)
        _deleteById((BeanIndexKey) key);
      else if (key instanceof BeanIdentifiersKey)
        _deleteByIdentifiers((BeanIdentifiersKey) key);
      else
        throw new UnsupportedOperationException("Key of type " + key.getClass().getName() + " not supported for deletion!");
    }
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

  /**
   * Updates changed bean values within a bean container by a {@link BeanIndexKey}.
   *
   * @param pKey           the index based bean key
   * @param pChangedValues the changed values mapped by bean fields
   */
  private void _updateById(BeanIndexKey pKey, Map<IField<?>, Object> pChangedValues)
  {
    builder.doUpdate(pUpdate -> pUpdate
        .table(pKey.getContainerId())
        .set(BeanColumnValueTuple.ofMap(pChangedValues))
        .whereId(pKey.getIndex())
        .update());
  }

  /**
   * Updates changed bean values within a bean container by a {@link BeanIdentifiersKey}.
   *
   * @param pKey           the identifier based bean key
   * @param pChangedValues the changed values mapped by bean fields
   */
  private void _updateByIdentifiers(BeanIdentifiersKey pKey, Map<IField<?>, Object> pChangedValues)
  {
    builder.doUpdate(pUpdate -> pUpdate
        .table(pKey.getContainerId())
        .set(BeanColumnValueTuple.ofMap(pChangedValues))
        .where(BeanWhereCondition.ofMap(pKey.getIdentifiers()))
        .update());
  }

  /**
   * Updates changed bean values within a persistent single bean.
   *
   * @param pBeanId        the id to identify the single bean data
   * @param pChangedValues the changed values mapped by bean fields
   */
  private void _updateSingleBean(String pBeanId, Map<IField<?>, Object> pChangedValues)
  {
    final IPersistenceModel model = models.getPersistenceModel(pBeanId);
    ((SingleBeanPersistenceModel) model).processChanges(pChangedValues, builder);
  }

  /**
   * Deletes bean data from a persistent bean container by a {@link BeanIndexKey}.
   *
   * @param pKey the index based bean key
   * @return <tt>true</tt> if the bean data has been deleted successfully
   */
  private boolean _deleteById(BeanIndexKey pKey)
  {
    return builder.doDelete(pDelete -> pDelete
        .from(pKey.getContainerId())
        .whereId(pKey.getIndex())
        .delete());
  }

  /**
   * Deletes bean data from a persistent bean container by a {@link BeanIdentifiersKey}.
   *
   * @param pKey the identifier based bean key
   * @return <tt>true</tt> if the bean data has been deleted successfully
   */
  private boolean _deleteByIdentifiers(BeanIdentifiersKey pKey)
  {
    return builder.doDelete(pDelete -> pDelete
        .from(pKey.getContainerId())
        .where(BeanWhereCondition.ofMap(pKey.getIdentifiers()))
        .delete());
  }
}
