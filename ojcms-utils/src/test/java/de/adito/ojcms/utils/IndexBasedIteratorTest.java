package de.adito.ojcms.utils;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link IndexBasedIterator}
 *
 * @author Simon Danner, 22.12.2018
 */
public class IndexBasedIteratorTest
{
  private List<Integer> list;
  private final IntConsumer remover = pIndex -> list.remove((int) pIndex);

  @BeforeEach
  public void initList()
  {
    list = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
  }

  @Test
  public void testSuccessfulIteration()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size).createIterator();
    _checkIterator(it, 0, 1, 2, 3);
  }

  @Test
  public void testStartIndex()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size) //
        .startAtIndex(1) //
        .createIterator();

    _checkIterator(it, 1, 2, 3);
  }

  @Test
  public void testEndIndex()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size) //
        .endBeforeIndex(3) //
        .createIterator();

    _checkIterator(it, 0, 1, 2);
  }

  @Test
  public void testNoSuchElement()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size) //
        .endBeforeIndex(0) //
        .createIterator();

    assertThrows(NoSuchElementException.class, it::next);

    final Iterator<Integer> it2 = IndexBasedIterator.buildIterator(list::get, list::size) //
        .endBeforeIndex(1) //
        .createIterator();

    assertThrows(NoSuchElementException.class, () ->
    {
      it2.next();
      it2.next();
    });
  }

  @Test
  public void testRemove()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size) //
        .withRemover(remover) //
        .createIterator();

    it.next(); //= 0
    it.remove(); //removed 0
    assertEquals(Arrays.asList(1, 2, 3), list);

    it.next(); //= 1
    it.next(); //= 2
    it.remove(); //removed 2
    assertEquals(Arrays.asList(1, 3), list);

    it.next(); //= 3
    assertThrows(NoSuchElementException.class, it::next);
  }

  @Test
  public void testRemoveIllegalState()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size) //
        .withRemover(remover) //
        .createIterator();

    assertThrows(IllegalStateException.class, it::remove);
    assertThrows(IllegalStateException.class, () ->
    {
      it.next();
      it.remove();
      it.remove();
    });
  }

  @Test
  public void testRemoveNoRemover()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size).createIterator();
    it.next();
    assertThrows(UnsupportedOperationException.class, it::remove);
  }

  @Test
  public void testConcurrentModificationFail()
  {
    final Iterator<Integer> it = IndexBasedIterator.buildIterator(list::get, list::size).createIterator();
    it.next();
    list.remove(2);
    assertThrows(ConcurrentModificationException.class, it::next);
  }

  /**
   * Tests an integer iterator by checking the iterated numbers against of given array of expected numbers.
   *
   * @param pIterator         the iterator to check
   * @param pExpectedElements the expected order of numbers to be returned by {@link Iterator#next()}
   */
  private static void _checkIterator(Iterator<Integer> pIterator, int... pExpectedElements)
  {
    Arrays.stream(pExpectedElements, 0, pExpectedElements.length) //
        .forEach(pExpectedElement ->
        {
          assertTrue(pIterator.hasNext());
          assertSame(pExpectedElement, pIterator.next());
        });
  }
}
