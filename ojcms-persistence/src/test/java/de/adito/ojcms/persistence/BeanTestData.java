package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.ojcms.transactions.api.*;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * Holds and stores bean data in memory for testing purposes.
 *
 * @author Simon Danner, 06.01.2020
 */
@ApplicationScoped
class BeanTestData
{
  private final Map<String, List<PersistentBeanData>> containers = new HashMap<>();
  private final Map<String, PersistentBeanData> singleBeans = new HashMap<>();

  /**
   * Registers a persistent container by id.
   *
   * @param pContainerId the container's id
   */
  void registerContainerType(String pContainerId)
  {
    containers.put(pContainerId, new ArrayList<>());
  }

  /**
   * Registers a persistent single bean by id.
   *
   * @param pBeanId   the id of the single bean
   * @param pBeanType the bean type of the persistent single bean
   */
  void registerSingleBeanType(String pBeanId, Class<? extends IBean<?>> pBeanType)
  {
    final Map<IField<?>, Object> initialContent = BeanReflector.reflectBeanFields(pBeanType).stream()
        .collect(HashMap::new, (pMap, pField) -> pMap.put(pField, pField.getInitialValue()), HashMap::putAll);

    singleBeans.put(pBeanId, new PersistentBeanData(-1, initialContent));
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
   * @param pNewBeanData a list of the new bean data to add to the container
   */
  void addToContainer(String pContainerId, List<PersistentBeanData> pNewBeanData)
  {
    if (!containers.containsKey(pContainerId))
      throw new IllegalArgumentException("Persistent container with id " + pContainerId + " does not exist!");

    containers.get(pContainerId).addAll(pNewBeanData);
  }

  /**
   * Removes multiple bean data from a persistent container.
   *
   * @param pKeys the container bean keys to identify the bean data to remove
   */
  void removeFromContainer(Set<IContainerBeanKey> pKeys)
  {
    for (IContainerBeanKey key : pKeys)
    {
      final List<PersistentBeanData> content = getContentForContainer(key.getContainerId());

      if (key instanceof BeanIndexKey)
        content.remove(((BeanIndexKey) key).getIndex());
      else if (key instanceof BeanIdentifiersKey)
      {
        final Iterator<PersistentBeanData> iterator = content.iterator();

        while (iterator.hasNext())
        {
          final PersistentBeanData data = iterator.next();
          if (data.getData().entrySet().containsAll(((BeanIdentifiersKey) key).getIdentifiers().entrySet()))
          {
            iterator.remove();
            break;
          }
        }
      }
      else
        throw new IllegalArgumentException("Unsupported container bean type " + key.getClass().getName());
    }
  }

  /**
   * Processes a change to some persistent bean data.
   *
   * @param pKey         the key identifying the persistent bean data
   * @param pChangedData a map containing every changed value by bean field
   */
  void processChange(IBeanKey pKey, Map<IField<?>, Object> pChangedData)
  {
    final String containerId = pKey.getContainerId();

    if (pKey instanceof BeanIndexKey)
    {
      final int index = ((BeanIndexKey) pKey).getIndex();
      final List<PersistentBeanData> containerContent = getContentForContainer(containerId);
      final PersistentBeanData newData = containerContent.get(index).integrateChanges(pChangedData);
      containerContent.set(index, newData);
    }
    else if (pKey instanceof BeanIdentifiersKey)
    {
      final List<PersistentBeanData> containerContent = getContentForContainer(containerId);
      final PersistentBeanData oldData = containerContent.stream()
          .filter(pBeanData -> pBeanData.getData().entrySet().containsAll(((BeanIdentifiersKey) pKey).getIdentifiers().entrySet()))
          .findAny()
          .orElseThrow(() -> new AssertionError("Bean not found for key " + pKey + " in container!"));

      final int index = containerContent.indexOf(oldData);
      containerContent.set(index, oldData.integrateChanges(pChangedData));
    }
    else if (pKey instanceof SingleBeanKey)
    {
      if (!singleBeans.containsKey(containerId))
        throw new IllegalArgumentException("Persistent single bean with id " + containerId + " does not exist!");

      final PersistentBeanData oldData = singleBeans.get(containerId);
      singleBeans.put(containerId, oldData.integrateChanges(pChangedData));
    }
    else
      throw new IllegalArgumentException("Key type " + pKey.getClass().getName() + " not supported for change integration!");
  }

  /**
   * Clears the test data.
   */
  void clear()
  {
    containers.clear();
    singleBeans.clear();
  }
}
