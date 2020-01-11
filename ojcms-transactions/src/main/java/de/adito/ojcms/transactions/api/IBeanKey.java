package de.adito.ojcms.transactions.api;

/**
 * Base interface for a key that identifies persistent bean data.
 *
 * @author Simon Danner, 31.12.2019
 */
public interface IBeanKey
{
  /**
   * The id of the container the bean is located in.
   *
   * @return the container id
   */
  String getContainerId();
}
