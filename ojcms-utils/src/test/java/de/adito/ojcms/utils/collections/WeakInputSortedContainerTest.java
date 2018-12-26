package de.adito.ojcms.utils.collections;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Special test for {@link WeakInputSortedContainer} to test the weak reference behaviour.
 * Interface based tests are already defined in {@link InputSortedElementsTest}.
 *
 * @author Simon Danner, 14.03.2018
 */
class WeakInputSortedContainerTest
{
  @Test
  public void testWeakReferences() throws InterruptedException
  {
    final WeakInputSortedContainer<Object> container = new WeakInputSortedContainer<>();
    container.add(new Object());
    System.gc();
    Thread.sleep(100); //NOSONAR
    assertTrue(container.isEmpty());
  }
}