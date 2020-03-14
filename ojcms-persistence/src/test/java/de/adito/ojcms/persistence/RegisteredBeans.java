package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.util.BeanReflector;
import de.adito.ojcms.transactions.api.PersistentBeanData;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * Manages through {@link Persist} registered persistent beans.
 *
 * @author Simon Danner, 29.02.2020
 */
@ApplicationScoped
class RegisteredBeans
{
  private final Set<String> containerIds = new HashSet<>();
  private final Map<String, PersistentBeanData> singleBeanInitialStates = new HashMap<>();

  /**
   * All registered persistent container ids.
   *
   * @return a list of bean container ids
   */
  Set<String> getContainerIds()
  {
    return new HashSet<>(containerIds);
  }

  /**
   * All initial states of registered single beans.
   *
   * @return a map describing the initial states of single beans by id
   */
  Map<String, PersistentBeanData> getSingleBeanInitialStates()
  {
    return new HashMap<>(singleBeanInitialStates);
  }

  /**
   * Registers a persistent container by id.
   *
   * @param pContainerId the container's id
   */
  void registerContainerType(String pContainerId)
  {
    containerIds.add(pContainerId);
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

    singleBeanInitialStates.put(pBeanId, new PersistentBeanData(-1, initialContent));
  }
}
