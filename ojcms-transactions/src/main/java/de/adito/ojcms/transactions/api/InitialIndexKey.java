package de.adito.ojcms.transactions.api;

/**
 * An index based container key that represent the initial state of a transaction.
 *
 * @author Simon Danner, 12.03.2020
 */
public class InitialIndexKey extends AbstractIndexKey
{
  /**
   * Initializes the key.
   *
   * @param pContainerId the id of the container the bean data is located in
   * @param pIndex       the index of the bean data
   */
  public InitialIndexKey(String pContainerId, int pIndex)
  {
    super(pContainerId, pIndex);
  }

  /**
   * Converts the initial index based key to an {@link CurrentIndexKey}.
   *
   * @param pCurrentIndex the current index of this initial key
   * @return the created current key
   */
  public CurrentIndexKey toCurrentKey(int pCurrentIndex)
  {
    return new CurrentIndexKey(getContainerId(), pCurrentIndex);
  }
}
