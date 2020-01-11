package de.adito.ojcms.cdi.context;

import de.adito.ojcms.cdi.ICdiControl;

/**
 * Control interface for an {@link AbstractCustomContext}.
 * Custom contexts may be started via {@link ICdiControl#startContext(Class)}.
 *
 * @author Simon Danner, 27.12.2019
 */
@FunctionalInterface
public interface IActiveContext
{
  /**
   * Destroys the currently active custom context. Releases CDI beans associated with the context's scope.
   * This method cannot be called twice.
   */
  void destroy();
}
