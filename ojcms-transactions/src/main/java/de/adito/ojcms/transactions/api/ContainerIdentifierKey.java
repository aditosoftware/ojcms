package de.adito.ojcms.transactions.api;

import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.IField;

import java.util.*;

/**
 * An unique key for {@link BeanData} within a container that is based on bean fields marked as {@link Identifier}.
 * The key combines the container's id and all values of identifier fields.
 *
 * @author Simon Danner, 26.12.2019
 */
public final class ContainerIdentifierKey
{
  private final String containerId;
  private final Map<IField<?>, Object> identifiers;

  /**
   * Initializes the key.
   *
   * @param pContainerId the id of the container the bean data is located in
   * @param pIdentifiers the identifier fields' values
   */
  public ContainerIdentifierKey(String pContainerId, Map<IField<?>, Object> pIdentifiers)
  {
    containerId = pContainerId;
    identifiers = pIdentifiers;
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

    final ContainerIdentifierKey that = (ContainerIdentifierKey) pOther;
    return Objects.equals(containerId, that.containerId) && identifiers.equals(that.identifiers);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(containerId, identifiers);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" +
        "containerId='" + containerId + '\'' +
        ", identifiers=" + identifiers +
        '}';
  }
}
