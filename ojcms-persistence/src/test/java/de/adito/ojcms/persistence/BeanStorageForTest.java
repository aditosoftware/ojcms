package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

/**
 * Implementation of {@link IBeanDataStorage} for tests.
 * Stores the bean data in memory while testing.
 *
 * @author Simon Danner, 06.01.2020
 */
@ApplicationScoped
class BeanStorageForTest implements IBeanDataStorage
{
  @Inject
  private BeanTestData data;

  @Override
  public void registerPersistentBean(Class<? extends IBean<?>> pBeanType, String pContainerId, boolean pIsContainer)
  {
    if (pIsContainer)
      data.registerContainerType(pContainerId);
    else
      data.registerSingleBeanType(pContainerId, pBeanType);
  }

  @Override
  public <KEY extends IBeanKey> void processChangesForBean(KEY pKey, Map<IField<?>, Object> pChangedValues)
  {
    data.processChange(pKey, pChangedValues);
  }

  @Override
  public void processAdditionsForContainer(String pContainerId, List<PersistentBeanData> pNewData)
  {
    data.addToContainer(pContainerId, pNewData);
  }

  @Override
  public void processRemovals(Set<IContainerBeanKey> pKeysToRemove)
  {
    data.removeFromContainer(pKeysToRemove);
  }

  @Override
  public void commitChanges()
  {
    data.clear();
  }

  @Override
  public void rollbackChanges()
  {
    data.clear();
  }
}
