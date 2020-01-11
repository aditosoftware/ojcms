package de.adito.ojcms.cdi.startup;

import de.adito.ojcms.cdi.*;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.ApplicationScoped;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link IStartupCallback} that should be called after CDI boot.
 *
 * @author Simon Danner, 02.01.2020
 */
public class StartupCallbackTest
{
  private static boolean called;

  @Test
  public void testCallbackRegisteredAndCalled()
  {
    called = false;
    final ICdiControl cdiControl = CdiContainer.boot();
    assertTrue(called);
    cdiControl.shutdown();
  }

  @SuppressWarnings("unused")
  @ApplicationScoped
  static class Callback implements IStartupCallback
  {
    @Override
    public void onCdiStartup()
    {
      called = true;
    }
  }
}
