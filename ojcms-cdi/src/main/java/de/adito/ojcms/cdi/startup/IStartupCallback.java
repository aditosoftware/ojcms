package de.adito.ojcms.cdi.startup;

/**
 * Defines an action called after CDI startup.
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

  /**
   * A number identifying the priority of this startup callback.
   * The default is zero. Callbacks with higher numbers will be executed earlier.
   *
   * @return a number indicating the priority of the startup callback
   */
  default int priority()
  {
    return 0;
  }
}
