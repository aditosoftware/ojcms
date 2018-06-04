package de.adito.beans.core.util;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;

/**
 * A index based iterator implementation.
 *
 * @param <ELEMENT> the elements provided by the iterator
 * @author Simon Danner, 28.02.2018
 */
public class IndexBasedIterator<ELEMENT> implements Iterator<ELEMENT>
{
  private int index;
  private int lastIndex = -1;
  private final int endIndexExclusive;
  private final Function<Integer, ELEMENT> provider;
  @Nullable
  private final Consumer<Integer> remover;

  /**
   * Creates a new iterator. (default start index 0).
   *
   * @param pEndIndexExclusive the end index (exclusive)
   * @param pProvider          a function providing an element at the current index
   */
  public IndexBasedIterator(int pEndIndexExclusive, Function<Integer, ELEMENT> pProvider)
  {
    this(0, pEndIndexExclusive, pProvider, null);
  }

  /**
   * Creates a new iterator (default start index 0).
   *
   * @param pEndIndexExclusive the end index (exclusive)
   * @param pProvider          a function providing an element at the current index
   * @param pRemover           an optional action to remove an element at the current index
   */
  public IndexBasedIterator(int pEndIndexExclusive, Function<Integer, ELEMENT> pProvider, @Nullable Consumer<Integer> pRemover)
  {
    this(0, pEndIndexExclusive, pProvider, pRemover);
  }

  /**
   * Creates a new iterator.
   *
   * @param pStartIndex        the start index
   * @param pEndIndexExclusive the end index (exclusive)
   * @param pProvider          a function providing an element at the current index
   */
  public IndexBasedIterator(int pStartIndex, int pEndIndexExclusive, Function<Integer, ELEMENT> pProvider)
  {
    this(pStartIndex, pEndIndexExclusive, pProvider, null);
  }

  /**
   * Creates a new iterator.
   *
   * @param pStartIndex        the start index
   * @param pEndIndexExclusive the end index (exclusive)
   * @param pProvider          a function providing an element at the current index
   * @param pRemover           an optional action to remove an element at the current index
   */
  public IndexBasedIterator(int pStartIndex, int pEndIndexExclusive, Function<Integer, ELEMENT> pProvider, @Nullable Consumer<Integer> pRemover)
  {
    index = pStartIndex;
    endIndexExclusive = pEndIndexExclusive;
    provider = pProvider;
    remover = pRemover;
  }

  @Override
  public boolean hasNext()
  {
    return index < endIndexExclusive;
  }

  @Override
  public ELEMENT next()
  {
    if (!hasNext())
      throw new NoSuchElementException();
    return provider.apply(lastIndex = index++);
  }

  @Override
  public void remove()
  {
    if (remover == null)
      throw new UnsupportedOperationException();
    if (lastIndex < 0)
      throw new IllegalStateException();
    remover.accept(index = lastIndex);
    lastIndex = -1;
  }
}
