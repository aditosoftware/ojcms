package de.adito.beans.core.util.weak;

import org.junit.jupiter.api.*;

/**
 * Special test for {@link WeakInputSortedContainer} to test the weak reference behaviour.
 * Interface based tests are already defined in {@link InputSortedElementsTest}.
 *
 * @author Simon Danner, 14.03.2018
 */
class WeakInputSortedContainerTest
{
  @Test
  public void testWeakReferences()
  {
    WeakInputSortedContainer<Object> container = new WeakInputSortedContainer<>();
    container.add(new Object());
    System.gc();
    try
    {
      Thread.sleep(100);
    }
    catch (InterruptedException pE)
    {
      throw new RuntimeException(pE);
    }
    Assertions.assertTrue(container.isEmpty());
  }
}