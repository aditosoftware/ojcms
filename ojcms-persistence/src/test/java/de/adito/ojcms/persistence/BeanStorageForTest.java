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
  private RegisteredBeansForTest registeredBeans;
  @Inject
  private BeanTestData data;

  @Override
  public void registerPersistentContainerBean(Class<? extends IBean> pBeanType, String pContainerId)
  {
    registeredBeans.registerContainerType(pContainerId);
  }

  @Override
  public void registerPersistentBaseTypeContainer(String pContainerId, Set<Class<? extends IBean>> pSubTypes)
  {
    registeredBeans.registerBaseContainerType(pContainerId);
  }

  @Override
  public void registerPersistentSingleBean(Class<? extends IBean> pBeanType, String pBeanId)
  {
    registeredBeans.registerSingleBeanType(pBeanId, pBeanType);
  }

  @Override
  public void processChangesForContainerBean(InitialIndexKey pKey, Map<IField<?>, Object> pChangedValues)
  {
    data.processChangeForContainerBean(pKey, pChangedValues);
  }

  @Override
  public void processChangesForSingleBean(SingleBeanKey pKey, Map<IField<?>, Object> pChangedValues)
  {
    data.processChangeForSingleBean(pKey, pChangedValues);
  }

  @Override
  public void processAdditionsForContainer(String pContainerId, Set<BeanAddition> pBeanAdditions)
  {
    data.addToContainer(pContainerId, pBeanAdditions);
  }

  @Override
  public void processRemovals(Map<String, Set<InitialIndexKey>> pKeysToRemoveByContainer)
  {
    data.removeFromContainer(pKeysToRemoveByContainer);
  }

  @Override
  public void commitChanges()
  {
  }

  @Override
  public void rollbackChanges()
  {
  }
}
