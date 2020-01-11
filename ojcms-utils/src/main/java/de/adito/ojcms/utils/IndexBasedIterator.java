package de.adito.ojcms.utils;

import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

/**
 * An index based iterator implementation.
 * Supports custom start and end indices and might be supplied with a remover function.
 *
 * @param <ELEMENT> the elements provided by the iterator
 * @author Simon Danner, 28.02.2018
 */
public final class IndexBasedIterator<ELEMENT> implements Iterator<ELEMENT>
{
  private final IntFunction<ELEMENT> elementProvider;
  private final IntSupplier sizeSupplier;
  @Nullable
  private IntConsumer remover;

  private int index;
  private int lastIndex = -1;
  private int endIndexExclusive;
  private int expectedSize;

  /**
   * Creates a builder to create the index based iterator.
   *
   * @param pElementProvider a function providing an element for a given index
   * @param pSizeProvider    provider for the size of the collection to iterate
   * @param <ELEMENT>        the elements provided by the iterator
   * @return a builder for the iterator. Use {@link Builder#createIterator()} to create it finally.
   */
  public static <ELEMENT> Builder<ELEMENT> buildIterator(IntFunction<ELEMENT> pElementProvider, IntSupplier pSizeProvider)
  {
    return new Builder<>(pElementProvider, pSizeProvider);
  }

  /**
   * Creates a new index based iterator.
   *
   * @param pElementProvider   a function providing an element for a given index
   * @param pSizeSupplier      a elementProvider for the current size of the data structure to iterate
   * @param pStartIndex        the start index
   * @param pEndIndexExclusive the end index (exclusive)
   * @param pRemover           an optional action to remove an element at a given index
   */
  private IndexBasedIterator(IntFunction<ELEMENT> pElementProvider, IntSupplier pSizeSupplier, int pStartIndex,
                             int pEndIndexExclusive, @Nullable IntConsumer pRemover)
  {
    index = pStartIndex;
    endIndexExclusive = pEndIndexExclusive;
    elementProvider = Objects.requireNonNull(pElementProvider);
    sizeSupplier = Objects.requireNonNull(pSizeSupplier);
    expectedSize = sizeSupplier.getAsInt();
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
    if (expectedSize != sizeSupplier.getAsInt())
      throw new ConcurrentModificationException();
    lastIndex = index;
    return elementProvider.apply(index++);
  }

  @Override
  public void remove()
  {
    if (remover == null)
      throw new UnsupportedOperationException();
    if (lastIndex < 0)
      throw new IllegalStateException();
    if (expectedSize != sizeSupplier.getAsInt())
      throw new ConcurrentModificationException();
    remover.accept(lastIndex);
    index = lastIndex;
    lastIndex = -1;
    endIndexExclusive--;
    expectedSize--;
  }

  /**
   * Builds the index based iterator.
   *
   * @param <ELEMENT> the elements provided by the iterator
   */
  public static class Builder<ELEMENT>
  {
    private final IntFunction<ELEMENT> elementProvider;
    private final IntSupplier sizeSupplier;
    private int start = -1;
    private int end = -1;
    @Nullable
    private IntConsumer remover;

    /**
     * Creates the builder initially with least required parameters
     *
     * @param pElementProvider a function providing an element for a given index
     * @param pSizeSupplier    a function providing an element for a given index
     */
    public Builder(IntFunction<ELEMENT> pElementProvider, IntSupplier pSizeSupplier)
    {
      elementProvider = pElementProvider;
      sizeSupplier = pSizeSupplier;
    }

    /**
     * Sets a certain start index for the iterator.
     *
     * @param pStartIndex the index to start the iteration from
     * @return the builder to enable a pipelining mechanism
     */
    public Builder<ELEMENT> startAtIndex(int pStartIndex)
    {
      start = pStartIndex;
      return this;
    }

    /**
     * Sets a certain end index for the iterator.
     * The index is considered as exclusive.
     *
     * @param pEndIndexExclusive the index to end the iteration before from (exclusive)
     * @return the builder to enable a pipelining mechanism
     */
    public Builder<ELEMENT> endBeforeIndex(int pEndIndexExclusive)
    {
      end = pEndIndexExclusive;
      return this;
    }

    /**
     * Adds a remover action to the iterator which enables {@link Iterator#remove()} to be supported.
     *
     * @param pRemover an action to remove an element at a given index
     * @return the builder to enable a pipelining mechanism
     */
    public Builder<ELEMENT> withRemover(@NotNull IntConsumer pRemover)
    {
      remover = Objects.requireNonNull(pRemover);
      return this;
    }

    /**
     * Creates the iterator finally.
     *
     * @return the newly created iterator based on this builder
     */
    public Iterator<ELEMENT> createIterator()
    {
      final int startIndex = start == -1 ? 0 : start;
      final int endIndex = end == -1 ? sizeSupplier.getAsInt() : end;
      return new IndexBasedIterator<>(elementProvider, sizeSupplier, startIndex, endIndex, remover);
    }
  }
}
