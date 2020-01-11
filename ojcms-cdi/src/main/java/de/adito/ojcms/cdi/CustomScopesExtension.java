package de.adito.ojcms.cdi;

import de.adito.ojcms.cdi.context.*;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

/**
 * CDI extension to register {@link AbstractCustomContext} instances with their scope annotations.
 *
 * @author Simon Danner, 25.12.2019
 */
class CustomScopesExtension implements Extension
{
  /**
   * Registers all scope annotation types of the custom context classes annotated by {@link CustomCdiContext}.
   *
   * @param pBeforeBeanDiscovery CDI element to register scopes before the CDI bean scan process
   */
  void beforeBeanDiscovery(@Observes BeforeBeanDiscovery pBeforeBeanDiscovery)
  {
    CdiContainer.getAllCustomScopeAnnotationTypes()
        .forEach(pAnnotationType -> pBeforeBeanDiscovery.addScope(pAnnotationType, true, false));
  }

  /**
   * Registers all custom context instances. Custom context classes must be annotated by {@link CustomCdiContext}.
   *
   * @param pAfterBeanDiscovery CDI element to add context instances after the CDI bean scan process
   */
  void afterBeanDiscovery(@Observes AfterBeanDiscovery pAfterBeanDiscovery)
  {
    CdiContainer.getAllCustomContextInstances().forEach(pAfterBeanDiscovery::addContext);
  }
}
