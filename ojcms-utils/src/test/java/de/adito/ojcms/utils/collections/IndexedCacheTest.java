package de.adito.ojcms.utils.collections;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Interface based test for {@link IIndexedCache}.
 * Add more actual sub types to this test if required.
 *
 * @author Simon Danner, 11.01.2020
 */
class IndexedCacheTest extends AbstractInterfaceTest<IIndexedCache>
{
  @Override
  protected Stream<IIndexedCache> getActualInstances()
  {
    return Stream.of(new MapBasedIndexedCache<_Element>(), new MapBasedIndexedCache<_Element>(10));
  }

  @TestAllSubTypes
  public void testAddElements(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    final _Element e3 = new _Element(666, "666");
    pCache.addAtIndex(e1, 9);
    pCache.addAtIndex(e2, 2);
    pCache.addAtIndex(e3, 0);

    assertEquals(3, pCache.size());
    assertSame(e1, pCache.getElementAtIndex(9).orElseThrow(AssertionError::new));
    assertSame(e2, pCache.getElementAtIndex(2).orElseThrow(AssertionError::new));
    assertSame(e3, pCache.getElementAtIndex(0).orElseThrow(AssertionError::new));
  }

  @TestAllSubTypes
  public void testAddElementAtExistingIndex(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    pCache.addAtIndex(e1, 5);
    pCache.addAtIndex(e2, 5);

    assertEquals(2, pCache.size());
    assertSame(e1, pCache.getElementAtIndex(6).orElseThrow(AssertionError::new));
    assertSame(e2, pCache.getElementAtIndex(5).orElseThrow(AssertionError::new));
  }

  @TestAllSubTypes
  public void testReplaceElement(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    pCache.addAtIndex(e1, 9);
    pCache.replaceAtIndex(e2, 9);

    assertEquals(1, pCache.size());
    assertSame(e2, pCache.getElementAtIndex(9).orElseThrow(AssertionError::new));
  }

  @TestAllSubTypes
  public void testRemoval(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    final _Element e3 = new _Element(666, "666");
    pCache.addAtIndex(e1, 9);
    pCache.addAtIndex(e2, 2);
    pCache.addAtIndex(e3, 4);

    final _Element removed = pCache.removeAtIndex(2).orElseThrow(AssertionError::new);
    assertEquals(2, pCache.size());
    assertSame(e2, removed);

    //The index of the first and third element must have been decreased
    assertEquals(8, pCache.indexOf(e1).orElseThrow(AssertionError::new));
    assertEquals(3, pCache.indexOf(e3).orElseThrow(AssertionError::new));

    //Check removal by hash identity
    pCache.removeElement(new _Element(666, "666"));
    assertEquals(1, pCache.size());

    pCache.removeAtIndex(7); //The index should have been decreased again
    assertTrue(pCache.isEmpty());
  }

  @TestAllSubTypes
  public void testSort(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    final _Element e3 = new _Element(666, "666");
    pCache.addAtIndex(e1, 9);
    pCache.addAtIndex(e2, 2);
    pCache.addAtIndex(e3, 4);

    pCache.sortElements(Comparator.comparing(pElement -> pElement.someValue, Comparator.reverseOrder()));
    assertEquals(3, pCache.size());
    assertSame(e3, pCache.getElementAtIndex(0).orElseThrow(AssertionError::new));
    assertSame(e2, pCache.getElementAtIndex(1).orElseThrow(AssertionError::new));
    assertSame(e1, pCache.getElementAtIndex(2).orElseThrow(AssertionError::new));
  }

  @TestAllSubTypes
  public void testClear(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    pCache.addAtIndex(e1, 9);
    pCache.addAtIndex(e2, 2);

    pCache.clear();
    assertTrue(pCache.isEmpty());
  }

  @TestAllSubTypes
  public void testComputeIfAbsent(IIndexedCache<_Element> pCache)
  {
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    pCache.addAtIndex(e1, 9);

    assertSame(e1, pCache.computeIfAbsent(9, pIndex -> new _Element(1, "1")));
    assertSame(e2, pCache.computeIfAbsent(5, pIndex -> e2));
    assertEquals(2, pCache.size());
  }

  @TestAllSubTypes
  public void testAdditionAtLimit(IIndexedCache<_Element> pCache)
  {
    final OptionalInt maxSize = pCache.getMaxSize();

    if (!maxSize.isPresent())
      return;

    final int lastIndex = maxSize.getAsInt() - 1;
    final _Element e1 = new _Element(42, "42");
    final _Element e2 = new _Element(123, "123");
    pCache.addAtIndex(e1, lastIndex);
    assertThrows(IllegalArgumentException.class, () -> pCache.addAtIndex(e2, lastIndex));
  }

  @Test
  public void testIsFull()
  {
    final IIndexedCache<_Element> cache = new MapBasedIndexedCache<>(2);
    assertFalse(cache.isFull());

    cache.addAtIndex(new _Element(1, "1"), 0);
    assertFalse(cache.isFull());

    cache.addAtIndex(new _Element(1, "1"), 1);
    assertTrue(cache.isFull());
  }

  /**
   * Some random element for testing purposes.
   */
  private static class _Element
  {
    private final int someValue;
    private final String anotherValue;

    _Element(int pSomeValue, String pAnotherValue)
    {
      someValue = pSomeValue;
      anotherValue = pAnotherValue;
    }

    @Override
    public boolean equals(Object pO)
    {
      if (this == pO)
        return true;
      if (pO == null || getClass() != pO.getClass())
        return false;

      final _Element element = (_Element) pO;
      return someValue == element.someValue && Objects.equals(anotherValue, element.anotherValue);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(someValue, anotherValue);
    }
  }
}
