package de.adito.beans.persistence;

import de.adito.beans.core.*;
import de.adito.beans.persistence.spi.IPersistentBeanDataStore;

import java.util.Collection;
import java.util.function.*;
import java.util.stream.*;

/**
 * Entry-Point for the bean persistence framework.
 * The user of the framework has to provide a {@link IPersistentBeanDataStore}, from which the persistent elements can be retrieved.
 *
 * @author Simon Danner, 16.02.2018
 */
public final class OJPersistence
{
  private static BeanDataStore beanDataStore = null;

  private OJPersistence()
  {
  }

  /**
   * Configures the persistence framework.
   * If the configuration is not given, the framework is not enabled.
   *
   * @param pDataStoreSupplier a function that supplies a persistent data store created by {@link DataStoreFactory}
   */
  public static void configure(Function<DataStoreFactory, IPersistentBeanDataStore> pDataStoreSupplier)
  {
    final IPersistentBeanDataStore dataStore;
    if (pDataStoreSupplier == null || (dataStore = pDataStoreSupplier.apply(new DataStoreFactory())) == null)
      throw new RuntimeException("A persistence data store must be given to enable the bean persistence framework!");

    beanDataStore = new BeanDataStore(dataStore);
    BeanCreationRegistry.listenByAnnotation(Persist.class, new _BeanCreationListener());
  }

  /**
   * Determines, if the persistence framework has been configured already.
   *
   * @return <tt>true</tt>, if the configuration is set
   */
  public static boolean isConfigured()
  {
    return beanDataStore != null;
  }

  /**
   * The bean data store to access the persistent data.
   */
  static BeanDataStore dataStore()
  {
    return beanDataStore;
  }

  /**
   * Removes all obsolete persistent single beans.
   * Has to be package protected to ensure this method cannot be accessed by the users of the framework.
   *
   * @param pStillExistingBeans all remaining single beans(to find the obsoletes)
   */
  static void removeObsoleteSingleBeans(Collection<IBean<?>> pStillExistingBeans)
  {
    beanDataStore.removeObsoleteSingleBeans(_getPersistentContainerIds(pStillExistingBeans.stream().map(Object::getClass)));
  }

  /**
   * Removes all obsolete persistent containers.
   * Has to be package protected to ensure this method cannot be accessed by the users of the framework.
   *
   * @param pStillExistingContainers all remaining containers (to find the obsoletes)
   */
  static void removeObsoleteBeanContainers(Collection<IBeanContainer<?>> pStillExistingContainers)
  {
    beanDataStore.removeObsoleteContainers(_getPersistentContainerIds(pStillExistingContainers.stream().map(IBeanContainer::getBeanType)));
  }

  /**
   * All persistent container ids of a stream of annotated elements.
   *
   * @param pAnnotatedElements the stream of annotated elements
   * @return the collection of persistent container ids
   */
  private static Collection<String> _getPersistentContainerIds(Stream<Class<?>> pAnnotatedElements)
  {
    return pAnnotatedElements
        .map(pElement -> pElement.getAnnotation(Persist.class).containerId())
        .collect(Collectors.toList());
  }

  /**
   * Listener for created persistent beans.
   * A persistent bean has to be annotated with {@link Persist}.
   * The listener adds a newly created bean (anywhere in the code) to the according persistent bean container.
   */
  private static class _BeanCreationListener implements BiConsumer<IBean<?>, Persist>
  {
    @SuppressWarnings("unchecked")
    @Override
    public void accept(IBean<?> pCreatedBean, Persist pAnnotation)
    {
      if (pAnnotation.mode() == EPersistenceMode.SINGLE || pAnnotation.storageMode() == EStorageMode.MANUAL)
        return;
      IBeanContainer container = beanDataStore.getContainerByPersistenceId(pAnnotation.containerId(), pCreatedBean.getClass());
      container.addBean(pCreatedBean);
    }
  }
}
