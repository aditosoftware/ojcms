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
    CONTAINER_BEAN_TYPES.forEach((pBeanType, pContainerId) -> storage.registerPersistentContainerBean(pBeanType, pContainerId));
    BASE_CONTAINER_TYPES.forEach((pBaseType, pRegistration) -> storage
        .registerPersistentBaseTypeContainer(pRegistration.getContainerId(), pRegistration.getSubTypes()));
    SINGLE_BEAN_TYPES.forEach((pBeanType, pBeanId) -> storage.registerPersistentSingleBean(pBeanType, pBeanId));
  }

  @Override
  public int priority()
  {
    return 100;
  }
}
