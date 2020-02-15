package de.adito.ojcms.cdi.context;

import de.adito.ojcms.cdi.*;
import org.junit.jupiter.api.*;

import javax.enterprise.context.*;
import java.lang.annotation.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for custom contexts.
 * They should be started correctly and beans that live in that scope should only be available then.
 *
 * @author Simon Danner, 02.01.2020
 */
public class CustomContextTest
{
  private static ICdiControl cdiControl;

  @BeforeAll
  public static void bootCdi()
  {
    cdiControl = CdiContainer.boot(pConfig -> pConfig.addBeanClasses(SomeBeanLivingInCustomContext.class));
  }

  @AfterAll
  public static void shutdownCdi()
  {
    cdiControl.shutdown();
  }

  @Test
  public void testCustomContext()
  {
    final IActiveContext activeContext = cdiControl.startContext(SomeScopeAnnotation.class);
    final SomeBeanLivingInCustomContext someBeanInScope = cdiControl.createInjected(SomeBeanLivingInCustomContext.class);
    assertEquals(42, someBeanInScope.returnMagic());
    activeContext.destroy();
  }

  @Test
  public void testExceptionNoContextStarted()
  {
    assertThrows(ContextNotActiveException.class, () -> cdiControl.createInjected(SomeBeanLivingInCustomContext.class).returnMagic());
  }

  @Test
  public void testExceptionAfterContextDestroy()
  {
    final IActiveContext activeContext = cdiControl.startContext(SomeScopeAnnotation.class);
    activeContext.destroy();
    assertThrows(ContextNotActiveException.class, () -> cdiControl.createInjected(SomeBeanLivingInCustomContext.class).returnMagic());
  }

  @CustomCdiContext
  public static class SomeCustomContext extends AbstractCustomContext
  {
    @Override
    public Class<? extends Annotation> getScope()
    {
      return SomeScopeAnnotation.class;
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  @NormalScope
  public @interface SomeScopeAnnotation
  {
  }

  @SomeScopeAnnotation
  public static class SomeBeanLivingInCustomContext
  {
    public int returnMagic()
    {
      return 42;
    }
  }
}
