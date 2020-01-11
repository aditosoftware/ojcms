package de.adito.ojcms.transactions.api;

import java.util.Objects;

/**
 * An unique key for {@link PersistentBeanData} within a container that is index based.
 * The key combines the container's id and the index of the bean data.
 *
 * @author Simon Danner, 26.12.2019
 */
public final class BeanIndexKey implements IContainerBeanKey
{
  private final String containerId;
  private final int index;

  /**
   * Initializes the key.
   *
   * @param pContainerId the id of the container the bean data is located in
   * @param pIndex       the index of the bean data
   */
  public BeanIndexKey(String pContainerId, int pIndex)
  {
    containerId = pContainerId;
    index = pIndex;
  }

  @Override
  public String getContainerId()
  {
    return containerId;
  }

  /**
   * The index of the bean within the container.
   *
   * @return the index of the bean
   */
  public int getIndex()
  {
    return index;
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;

    final BeanIndexKey that = (BeanIndexKey) pOther;
    return index == that.index && Objects.equals(containerId, that.containerId);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(containerId, index);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" +
        "containerId='" + containerId + '\'' +
        ", index=" + index +
        '}';
  }
}
