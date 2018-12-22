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
  private final Function<Integer, ELEMENT> provider;
  private final Supplier<Integer> sizeProvider;
  @Nullable
  private Consumer<Integer> remover;

  private int index;
  private int lastIndex = -1;
  private int endIndexExclusive;
  private int expectedSize;

  /**
   * Creates a builder to create the index based iterator.
   *
   * @param pElementProvider a function providing an element for a given index
   * @param pSizeProvider    a function providing an element for a given index
   * @param <ELEMENT>        the elements provided by the iterator
   * @return a builder for the iterator. Use {@link Builder#createIterator()} to create it finally.
   */
  public static <ELEMENT> Builder<ELEMENT> buildIterator(Function<Integer, ELEMENT> pElementProvider, Supplier<Integer> pSizeProvider)
  {
    return new Builder<>(pElementProvider, pSizeProvider);
  }

  /**
   * Creates a new index based iterator.
   *
   * @param pElementProvider   a function providing an element for a given index
   * @param pSizeProvider      a provider for the current size of the data structure to iterate
   * @param pStartIndex        the start index
   * @param pEndIndexExclusive the end index (exclusive)
   * @param pRemover           an optional action to remove an element at a given index
   */
  private IndexBasedIterator(Function<Integer, ELEMENT> pElementProvider, Supplier<Integer> pSizeProvider, int pStartIndex,
                             int pEndIndexExclusive, @Nullable Consumer<Integer> pRemover)
  {
    index = pStartIndex;
    endIndexExclusive = pEndIndexExclusive;
    provider = Objects.requireNonNull(pElementProvider);
    sizeProvider = Objects.requireNonNull(pSizeProvider);
    expectedSize = sizeProvider.get();
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
    if (expectedSize != sizeProvider.get())
      throw new ConcurrentModificationException();
    return provider.apply(lastIndex = index++);
  }

  @Override
  public void remove()
  {
    if (remover == null)
      throw new UnsupportedOperationException();
    if (lastIndex < 0)
      throw new IllegalStateException();
    if (expectedSize != sizeProvider.get())
      throw new ConcurrentModificationException();
    remover.accept(index = lastIndex);
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
    private final Function<Integer, ELEMENT> elementProvider;
    private final Supplier<Integer> sizeSupplier;
    private int start = -1, end = -1;
    @Nullable
    private Consumer<Integer> remover;

    /**
     * Creates the builder initially with least required parameters
     *
     * @param pElementProvider a function providing an element for a given index
     * @param pSizeProvider    a function providing an element for a given index
     */
    public Builder(Function<Integer, ELEMENT> pElementProvider, Supplier<Integer> pSizeProvider)
    {
      elementProvider = pElementProvider;
      sizeSupplier = pSizeProvider;
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
    public Builder<ELEMENT> withRemover(@NotNull Consumer<Integer> pRemover)
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
      return new IndexBasedIterator<>(elementProvider, sizeSupplier, start == -1 ? 0 : start, end == -1 ? sizeSupplier.get() : end, remover);
    }
  }
}
