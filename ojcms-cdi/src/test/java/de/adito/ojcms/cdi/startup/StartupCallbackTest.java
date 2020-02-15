package de.adito.ojcms.cdi.startup;

import de.adito.ojcms.cdi.*;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.ApplicationScoped;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link IStartupCallback} that should be called after CDI boot.
 *
 * @author Simon Danner, 02.01.2020
 */
public class StartupCallbackTest
{
  private static List<Class<? extends IStartupCallback>> calledOrder;

  @Test
  public void testCallbackRegisteredAndCalled()
  {
    calledOrder = new ArrayList<>();
    final ICdiControl cdiControl = CdiContainer.boot(pConfig -> pConfig.addBeanClasses(Callback.class, CallbackWithHigherPriority.class));
    assertEquals(2, calledOrder.size());
    assertSame(CallbackWithHigherPriority.class, calledOrder.get(0));
    assertSame(Callback.class, calledOrder.get(1));
    cdiControl.shutdown();
  }

  @SuppressWarnings("unused")
  @ApplicationScoped
  static class Callback implements IStartupCallback
  {
    @Override
    public void onCdiStartup()
    {
      calledOrder.add(Callback.class);
    }
  }

  @SuppressWarnings("unused")
  @ApplicationScoped
  static class CallbackWithHigherPriority implements IStartupCallback
  {
    @Override
    public void onCdiStartup()
    {
      calledOrder.add(CallbackWithHigherPriority.class);
    }

    @Override
    public int priority()
    {
      return 1;
    }
  }
}
