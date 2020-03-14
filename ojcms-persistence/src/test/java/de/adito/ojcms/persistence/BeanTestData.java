package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.cdi.TestMethodScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.exceptions.BeanDataNotFoundException;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Holds and stores bean data in memory for testing purposes.
 *
 * @author Simon Danner, 06.01.2020
 */
@TestMethodScoped
class BeanTestData
{
  private final Map<String, List<PersistentBeanData>> containers;
  private final Map<String, PersistentBeanData> singleBeans;

  @Inject
  BeanTestData(RegisteredBeans registeredBeans)
  {
    containers = registeredBeans.getContainerIds().stream()
        .collect(Collectors.toMap(Function.identity(), pId -> new ArrayList<>()));
    singleBeans = registeredBeans.getSingleBeanInitialStates();
  }

  /**
   * Resolves the content of a persistent bean container.
   *
   * @param pContainerId the id of the persistent container
   * @return a list of {@link PersistentBeanData}
   */
  List<PersistentBeanData> getContentForContainer(String pContainerId)
  {
    if (!containers.containsKey(pContainerId))
      throw new IllegalArgumentException("Persistent container with id " + pContainerId + " does not exist!");

    return containers.get(pContainerId);
  }

  /**
   * Resolves the {@link PersistentBeanData} for a single bean.
   *
   * @param pBeanId the id of the persistent single bean
   * @return the persistent bean data
   */
  PersistentBeanData getContentForSingleBean(String pBeanId)
  {
    if (!singleBeans.containsKey(pBeanId))
      throw new IllegalArgumentException("Persistent single bean with id " + pBeanId + " does not exist!");

    return singleBeans.get(pBeanId);
  }

  /**
   * Adds new {@link PersistentBeanData} to a persistent bean container.
   *
   * @param pContainerId the id of the persistent container
   * @param pNewBeanData a set of the new bean data to add to the container
   */
  void addToContainer(String pContainerId, Set<PersistentBeanData> pNewBeanData)
  {
    if (!containers.containsKey(pContainerId))
      throw new IllegalArgumentException("Persistent container with id " + pContainerId + " does not exist!");

    containers.get(pContainerId).addAll(pNewBeanData);
  }

  /**
   * Removes multiple bean data from a persistent container.
   *
   * @param pKeysToRemoveByContainer mapped by container ids bean keys to remove
   */
  void removeFromContainer(Map<String, Set<InitialIndexKey>> pKeysToRemoveByContainer)
  {
    pKeysToRemoveByContainer.forEach((pContainerId, pKeysToRemove) ->
                                     {
                                       final List<PersistentBeanData> content = getContentForContainer(pContainerId);
                                       final Set<Integer> indexes = pKeysToRemove.stream().map(InitialIndexKey::getIndex).collect(toSet());
                                       content.removeIf(pData -> indexes.contains(pData.getIndex()));
                                     });
  }

  /**
   * Processes changes for some persistent container bean data.
   *
   * @param pKey         the key identifying the persistent bean data by index
   * @param pChangedData a map containing every changed value by bean field
   */
  void processChangeForContainerBean(InitialIndexKey pKey, Map<IField<?>, Object> pChangedData)
  {
    final int index = pKey.getIndex();
    final List<PersistentBeanData> containerContent = getContentForContainer(pKey.getContainerId());
    final PersistentBeanData newData = containerContent.get(index).integrateChanges(pChangedData);
    containerContent.set(index, newData);
  }

  /**
   * Processes changes for some persistent single bean.
   *
   * @param pKey         the key identifying the persistent single bean
   * @param pChangedData a map containing every changed value by bean field
   */
  void processChangeForSingleBean(SingleBeanKey pKey, Map<IField<?>, Object> pChangedData)
  {
    final String beanId = pKey.getBeanId();

    if (!singleBeans.containsKey(beanId))
      throw new BeanDataNotFoundException(pKey);

    final PersistentBeanData oldData = singleBeans.get(beanId);
    singleBeans.put(beanId, oldData.integrateChanges(pChangedData));
  }
}
