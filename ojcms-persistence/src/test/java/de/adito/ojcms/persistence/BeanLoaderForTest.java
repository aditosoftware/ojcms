package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.exceptions.BeanDataNotFoundException;
import de.adito.ojcms.transactions.spi.IBeanDataLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Implementation of {@link IBeanDataLoader} for tests.
 * Holds the bean data in memory while testing.
 *
 * @author Simon Danner, 06.01.2020
 */
@ApplicationScoped
class BeanLoaderForTest implements IBeanDataLoader
{
  @Inject
  private BeanTestData data;
  @Inject
  private RegisteredBeansForTest registeredBeans;

  @Override
  public int loadContainerSize(String pContainerId)
  {
    return data.getContentForContainer(pContainerId).size();
  }

  @Override
  public PersistentBeanData loadContainerBeanDataByIndex(InitialIndexKey pKey)
  {
    return _findByIndex(pKey).orElseThrow(() -> new BeanDataNotFoundException(pKey));
  }

  @Override
  public <BEAN extends IBean> Class<BEAN> loadBeanTypeWithinContainer(InitialIndexKey pKey)
  {
    if (!registeredBeans.getBaseContainerIds().contains(pKey.getContainerId()))
      throw new IllegalArgumentException("The container with id " + pKey.getContainerId() + " is not a base container!");

    //noinspection unchecked
    return (Class<BEAN>) _findByIndex(pKey) //
        .map(BeanAddition::getBeanType) //
        .orElseThrow(() -> new BeanDataNotFoundException(pKey));
  }

  @Override
  public Optional<PersistentBeanData> loadContainerBeanDataByIdentifiers(String pContainerId, Map<IField<?>, Object> pIdentifiers)
  {
    return _findInContainer(pContainerId, pData -> pData.getData().entrySet().containsAll(pIdentifiers.entrySet())) //
        .map(pData -> pData);
  }

  @Override
  public PersistentBeanData loadSingleBeanData(SingleBeanKey pKey)
  {
    return data.getContentForSingleBean(pKey.getBeanId());
  }

  @Override
  public Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId)
  {
    return data.getContentForContainer(pContainerId).stream() //
        .collect(Collectors.toMap(PersistentBeanData::getIndex, identity()));
  }

  /**
   * Tries to resolve some {@link PersistentBeanData} from a specific container by index.
   *
   * @param pIndexKey the index based key to identify the bean data
   * @return the optionally resolved persistent data applying to the predicate
   */
  private Optional<BeanAddition> _findByIndex(InitialIndexKey pIndexKey)
  {
    return _findInContainer(pIndexKey.getContainerId(), pData -> pData.getIndex() == pIndexKey.getIndex());
  }

  /**
   * Tries to resolve some {@link PersistentBeanData} in a specific container based on a given predicate.
   *
   * @param pContainerId the id of the container the bean data is located in
   * @param pPredicate   the predicate to find the requested persistent bean data
   * @return the optionally resolved persistent data applying to the predicate
   */
  private Optional<BeanAddition> _findInContainer(String pContainerId, Predicate<BeanAddition> pPredicate)
  {
    return data.getContentForContainer(pContainerId).stream() //
        .filter(pPredicate) //
        .findAny();
  }
}
