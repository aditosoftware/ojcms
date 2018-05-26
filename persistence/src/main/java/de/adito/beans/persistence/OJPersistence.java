package de.adito.beans.persistence;

import de.adito.beans.core.*;
import de.adito.beans.persistence.spi.IPersistentBeanDataStore;

import java.util.function.*;

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
   * Sets the configuration for the persistence framework.
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
   * The bean data store to access to persistent data.
   */
  static BeanDataStore dataStore()
  {
    return beanDataStore;
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
      IBeanContainer container = beanDataStore.getContainerByPersistenceId(pAnnotation.containerId(), pCreatedBean.getClass());
      if (!container.contains(pCreatedBean))
        container.addBean(pCreatedBean);
    }
  }
}
