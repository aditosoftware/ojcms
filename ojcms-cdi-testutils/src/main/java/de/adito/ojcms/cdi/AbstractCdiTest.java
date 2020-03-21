package de.adito.ojcms.cdi;

import de.adito.ojcms.cdi.context.IActiveContext;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for CDI based unit tests.
 * Starts the CDI container before the execution of a test class.
 * Starts all custom contexts per test case and also performs injection on the test class per test case.
 * Terminates the CDI container at the end of a test class.
 *
 * @author Simon Danner, 25.12.2019
 */
public abstract class AbstractCdiTest
{
  protected static ICdiControl cdiControl;
  private List<IActiveContext> customContextList;

  @BeforeAll
  public static void initCdiContainer()
  {
    cdiControl = CdiContainer.boot();
  }

  @BeforeEach
  public void setupCdi()
  {
    //Start all custom contexts in classpath
    customContextList = CdiContainer.getAllCustomScopeAnnotationTypes().stream() //
        .map(pScopeAnnotationType -> cdiControl.startContext(pScopeAnnotationType)) //
        .collect(Collectors.toList());

    cdiControl.injectInstance(this);
  }

  @AfterEach
  public void stopContext()
  {
    customContextList.forEach(IActiveContext::destroy);
  }

  @AfterAll
  public static void stopCdiContainer()
  {
    if (cdiControl != null)
      cdiControl.shutdown();
  }
}
