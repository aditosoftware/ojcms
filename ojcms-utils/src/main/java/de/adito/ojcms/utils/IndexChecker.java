package de.adito.ojcms.utils;

import java.util.function.Supplier;

/**
 * Index checker utility for index based data structures.
 *
 * @author Simon Danner, 24.12.2018
 */
public final class IndexChecker
{
  private final Supplier<Integer> sizeSupplier;

  /**
   * Creates a check instance for multiple usages.
   *
   * @param pCurrentSizeSupplier a supplier for the current size of the data structure (max index = size - 1)
   * @return an index checker instance
   */
  public static IndexChecker create(Supplier<Integer> pCurrentSizeSupplier)
  {
    return new IndexChecker(pCurrentSizeSupplier);
  }

  /**
   * Static entry point to check indices. Should be used for one time usage.
   *
   * @param pCurrentSizeSupplier a supplier for the current size of the data structure (max index = size - 1)
   * @param pIndex               the index to check
   * @return the checked index
   * @throws IndexOutOfBoundsException if the index is out of it bounds
   */
  public static int check(Supplier<Integer> pCurrentSizeSupplier, int pIndex)
  {
    return create(pCurrentSizeSupplier).check(pIndex);
  }

  /**
   * Creates an index checker instance.
   *
   * @param pSizeSupplier supplier for the current size of the data structure (max index = size - 1)
   */
  private IndexChecker(Supplier<Integer> pSizeSupplier)
  {
    sizeSupplier = pSizeSupplier;
  }

  /**
   * Checks an index. Must be between 0 (inclusive) and the current size of the data structure (exclusive).
   *
   * @param pIndex the index to check
   * @return the checked index
   * @throws IndexOutOfBoundsException if the index is out of it bounds
   */
  public int check(int pIndex)
  {
    final int maxIndex = sizeSupplier.get() - 1;
    if (pIndex < 0 || pIndex > maxIndex)
      throw new IndexOutOfBoundsException("index: " + pIndex + " max index: " + maxIndex);
    return pIndex;
  }
}
