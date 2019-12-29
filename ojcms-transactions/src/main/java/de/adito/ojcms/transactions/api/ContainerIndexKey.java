package de.adito.ojcms.transactions.api;

import java.util.Objects;

/**
 * An unique key for {@link BeanData} within a container that is index based.
 * The key combines the container's id and the index of the bean data.
 *
 * @author Simon Danner, 26.12.2019
 */
public final class ContainerIndexKey
{
  private final String containerId;
  private final int index;

  /**
   * Initializes the key.
   *
   * @param pContainerId the id of the container the bean data is located in
   * @param pIndex       the index of the bean data
   */
  public ContainerIndexKey(String pContainerId, int pIndex)
  {
    containerId = pContainerId;
    index = pIndex;
  }

  /**
   * The id of the container the bean data associated with this key is located in.
   *
   * @return the container id
   */
  public String getContainerId()
  {
    return containerId;
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;

    final ContainerIndexKey that = (ContainerIndexKey) pOther;
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
