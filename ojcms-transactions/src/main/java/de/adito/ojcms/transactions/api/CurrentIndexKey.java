package de.adito.ojcms.transactions.api;

/**
 * An index based container key that represent the current state of a transaction.
 *
 * @author Simon Danner, 12.03.2020
 */
public class CurrentIndexKey extends AbstractIndexKey
{
  /**
   * Initializes the key.
   *
   * @param pContainerId the id of the container the bean data is located in
   * @param pIndex       the index of the bean data
   */
  public CurrentIndexKey(String pContainerId, int pIndex)
  {
    super(pContainerId, pIndex);
  }

  /**
   * Converts the current index based key to an {@link InitialIndexKey}.
   *
   * @param pInitialIndex the initial index of this current key
   * @return the created initial key
   */
  public InitialIndexKey toInitialKey(int pInitialIndex)
  {
    return new InitialIndexKey(getContainerId(), pInitialIndex);
  }
}
