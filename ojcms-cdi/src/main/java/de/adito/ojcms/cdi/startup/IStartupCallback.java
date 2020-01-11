package de.adito.ojcms.cdi.startup;

/**
 * Defines an action called after CDI startup.
 * Such callbacks are executed in any order.
 *
 * @author Simon Danner, 02.01.2020
 */
@FunctionalInterface
public interface IStartupCallback
{
  /**
   * Called after successful CDI initialization.
   */
  void onCdiStartup();
}
