package org.glassfish.jersey.inject.cdi.se;

import de.adito.ojcms.cdi.*;
import de.adito.ojcms.rest.security.AuthenticationRestService;
import org.glassfish.jersey.inject.cdi.se.injector.ContextInjectionResolverImpl;
import org.glassfish.jersey.internal.inject.*;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.se.*;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import java.util.Set;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * With this specialized {@link InjectionManagerFactory} we are able to use {@link CdiContainer} for "jersey-cdi2-se" (Weld-Jersey bridge)
 * This class also fixes an issue of "jersey-cdi2-se" where interceptors won't be enabled for non Jersey beans.
 *
 * @author Simon Danner, 24.01.2020
 */
@Priority(30)
public class RestInjectionManagerFactory implements InjectionManagerFactory
{
  private static final Logger LOGGER = Logger.getLogger(RestInjectionManagerFactory.class.getName());

  @Override
  public InjectionManager create(Object pParent)
  {
    return new RestInjectionManager();
  }

  /**
   * This specialized {@link CdiSeInjectionManager} boots our {@link CdiContainer} instead of the provided Weld one.
   */
  @Vetoed
  private static class RestInjectionManager extends CdiSeInjectionManager
  {
    private ICdiControl cdiControl;

    @Override
    public void completeRegistration() throws IllegalStateException
    {
      final AbstractBinder bindings = getBindings();
      bindings.bind(Bindings.service(this).to(InjectionManager.class));
      bindings.install(new ContextInjectionResolverImpl.Binder(this::getBeanManager));

      if (CdiContainer.isBooted())
        LOGGER.warning("CDI container already booted! Jersey completed registration multiple times!");

      final Consumer<SeContainerInitializer> config = pConfig -> pConfig.addExtensions(new RestBeanRegisterExtension(bindings));
      cdiControl = CdiContainer.isBooted() ? CdiContainer.forceAdditionalBoot(config) : CdiContainer.boot(config);
      final SeContainer container = cdiControl.getContainer();
      setContainer(container);
      setBeanManager(container.getBeanManager());
    }

    @Override
    public void register(Binding binding)
    {
      //Exclude auth rest service as class binding, we have to figure out why is there...
      if (binding.getImplementationType() == AuthenticationRestService.class && binding instanceof ClassBinding)
        return;

      super.register(binding);
    }

    @Override
    public void shutdown()
    {
      cdiControl.shutdown();
    }
  }

  /**
   * With this specialized register extension non jersey injection targets will be excluded from exchanging the injection target's instance,
   * which somehow breaks interceptors from working.
   */
  @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
  private static class RestBeanRegisterExtension extends SeBeanRegisterExtension
  {
    private final Set<Class> jerseyTypes;

    RestBeanRegisterExtension(AbstractBinder pBindings)
    {
      super(pBindings);
      jerseyTypes = pBindings.getBindings().stream() //
          .map((Function<Binding, Class>) Binding::getImplementationType) //
          .collect(Collectors.toSet());
    }

    @Override
    public <T> void observeInjectionTarget(@Observes ProcessInjectionTarget<T> pInjectionTarget)
    {
      if (!jerseyTypes.contains(pInjectionTarget.getAnnotatedType().getJavaClass()))
        return;

      super.observeInjectionTarget(pInjectionTarget);
    }
  }
}
