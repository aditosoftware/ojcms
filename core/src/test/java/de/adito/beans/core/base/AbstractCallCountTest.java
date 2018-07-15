package de.adito.beans.core.base;

import org.junit.jupiter.api.*;

/**
 * Abstract base class for every test class, that has to check some amount of method calls.
 * This could be very useful, where a method call leads to an indirect call of something to test (e.g. listeners).
 *
 * @author Simon Danner, 15.07.2018
 */
public abstract class AbstractCallCountTest
{
  private int callCount;

  @BeforeEach
  public void resetCallCount()
  {
    callCount = 0;
  }

  @AfterEach
  public void checkCallCount(TestInfo pTestInfo)
  {
    pTestInfo.getTestMethod()
        .ifPresent(pMethod -> {
          if (pMethod.isAnnotationPresent(CallCount.class))
            Assertions.assertEquals(pMethod.getAnnotation(CallCount.class).expectedCallCount(), callCount);
        });
  }

  /**
   * Increments the call count.
   */
  protected void called()
  {
    callCount++;
  }
}
