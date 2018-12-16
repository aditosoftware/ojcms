package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.persistence.datastores.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Entry-Point for the bean persistence framework.
 * The user of the framework has to provide a {@link IPersistentSourcesStore} from which the persistent data sources can be retrieved.
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
   * @param pDataStoreSupplier a function that supplies a persistent data store created by {@link PersistentDataSourcesFactory}
   */
  public static void configure(Function<PersistentDataSourcesFactory, IPersistentSourcesStore> pDataStoreSupplier)
  {
    final PersistentDataSourcesFactory factory = new PersistentDataSourcesFactory(() -> beanDataStore);
    beanDataStore = new BeanDataStore(Objects.requireNonNull(pDataStoreSupplier).apply(factory));
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
   * The bean data store to access the persistent data sources.
   *
   * @return a data store for persistent data sources
   */
  static BeanDataStore dataStore()
  {
    return beanDataStore;
  }

  /**
   * Removes all obsolete persistent single bean data sources.
   * Has to be package protected to ensure this method cannot be accessed by the users of the framework.
   *
   * @param pStillExistingBeans all remaining single bean data sources (to find the obsoletes)
   */
  static void removeObsoleteSingleBeans(Collection<IBean<?>> pStillExistingBeans)
  {
    beanDataStore.removeObsoleteSingleBeans(pStillExistingBeans);
  }

  /**
   * Removes all obsolete persistent container data sources.
   * Has to be package protected to ensure this method cannot be accessed by the users of the framework.
   *
   * @param pStillExistingContainers all remaining container data sources (to find the obsoletes)
   */
  static void removeObsoleteBeanContainers(Collection<IBeanContainer<?>> pStillExistingContainers)
  {
    beanDataStore.removeObsoleteContainers(_getPersistentContainerIds(pStillExistingContainers.stream().map(IBeanContainer::getBeanType)));
  }

  /**
   * All persistent container ids of a stream of annotated elements. Refers to {@link Persist#containerId()}.
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
   * A persistent bean data source has to be annotated with {@link Persist}.
   * The listener adds a newly created bean (anywhere in the code) to the according persistent bean container
   * if the storage mode is {@link EStorageMode#AUTOMATIC}.
   */
  private static class _BeanCreationListener implements BiConsumer<IBean<?>, Persist>
  {
    @SuppressWarnings("unchecked")
    @Override
    public void accept(IBean<?> pCreatedBean, Persist pAnnotation)
    {
      if (pAnnotation.mode() == EPersistenceMode.SINGLE || pAnnotation.storageMode() == EStorageMode.MANUAL)
        return;
      final IBeanContainer container = beanDataStore.getContainerByPersistenceId(pAnnotation.containerId(), pCreatedBean.getClass());
      container.addBean(pCreatedBean);
    }
  }
}
