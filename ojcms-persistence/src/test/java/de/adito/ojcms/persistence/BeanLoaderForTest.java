package de.adito.ojcms.persistence;

import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataLoader;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
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

  @Override
  public int loadContainerSize(String pContainerId)
  {
    return data.getContentForContainer(pContainerId).size();
  }

  @Override
  public <KEY extends IBeanKey> PersistentBeanData loadByKey(KEY pKey)
  {
    if (pKey instanceof BeanIndexKey)
      return _findInContainer(pKey, pPersistentBeanData -> pPersistentBeanData.getIndex() == ((BeanIndexKey) pKey).getIndex());
    else if (pKey instanceof BeanIdentifiersKey)
      return _findInContainer(pKey, pData -> pData.getData().entrySet().containsAll(((BeanIdentifiersKey) pKey).getIdentifiers().entrySet()));
    else if (pKey instanceof SingleBeanKey)
      return data.getContentForSingleBean(pKey.getContainerId());
    else
      throw new IllegalArgumentException("Key type " + pKey.getClass().getName() + " not supported!");
  }

  @Override
  public Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId)
  {
    return data.getContentForContainer(pContainerId).stream()
        .collect(Collectors.toMap(PersistentBeanData::getIndex, identity()));
  }

  /**
   * Resolves some {@link PersistentBeanData} in a specific container based on a given predicate.
   *
   * @param pKey       the bean key to resolve the container id from
   * @param pPredicate the predicate to find the requested persistent bean data
   * @return the persistent data applying to the predicate
   */
  private PersistentBeanData _findInContainer(IBeanKey pKey, Predicate<PersistentBeanData> pPredicate)
  {
    return data.getContentForContainer(pKey.getContainerId()).stream()
        .filter(pPredicate)
        .findAny()
        .orElseThrow(() -> new AssertionError("Bean not found for key " + pKey + " in container!"));
  }
}
