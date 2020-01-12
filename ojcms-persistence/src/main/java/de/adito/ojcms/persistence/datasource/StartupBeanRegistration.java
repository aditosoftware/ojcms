package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.cdi.startup.IStartupCallback;
import de.adito.ojcms.transactions.spi.IBeanDataStorage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static de.adito.ojcms.persistence.datasource.BeanProducerExtension.*;

/**
 * Registers all persistent container and single bean types at the persistent storage system on CDI startup.
 *
 * @author Simon Danner, 09.01.2020
 */
@ApplicationScoped
class StartupBeanRegistration implements IStartupCallback
{
  @Inject
  private IBeanDataStorage storage;

  @Override
  public void onCdiStartup()
  {
    CONTAINER_BEAN_TYPES.forEach((pBeanType, pContainerId) -> storage.registerPersistentBean(pBeanType, pContainerId, true));
    SINGLE_BEAN_TYPES.forEach((pBeanType, pBeanId) -> storage.registerPersistentBean(pBeanType, pBeanId, false));
  }
}