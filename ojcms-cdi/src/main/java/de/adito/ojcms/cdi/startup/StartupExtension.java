package de.adito.ojcms.cdi.startup;

import javax.enterprise.context.*;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.util.*;

/**
 * CDI extension that identifies {@link IStartupCallback} types and executes them after successful CDI boot.
 *
 * @author Simon Danner, 02.01.2020
 */
class StartupExtension implements Extension
{
  private final Set<Class<? extends IStartupCallback>> startupCallbacks = new HashSet<>();

  /**
   * Resolves all types that define a {@link IStartupCallback}.
   *
   * @param pProcessedBean the currently processed CDI bean
   */
  void findCallbacks(@Observes ProcessBean<? extends IStartupCallback> pProcessedBean)
  {
    //noinspection unchecked
    startupCallbacks.add((Class<? extends IStartupCallback>) pProcessedBean.getAnnotated().getBaseType());
  }

  /**
   * This function will be called when CDI boot is finished.
   * Calls every startup callback.
   */
  void afterCdiStartup(@Observes @Initialized(ApplicationScoped.class) Object pObject)
  {
    startupCallbacks.forEach(pCallbackType -> CDI.current().select(pCallbackType).get().onCdiStartup());
  }
}
