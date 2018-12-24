package de.adito.ojcms.beans.exceptions.container;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;

/**
 * Thrown, if the limit of a bean container is reached.
 *
 * @author Simon Danner, 23.12.2018
 */
public class BeanContainerLimitReachedException extends OJRuntimeException
{
  /**
   * Creates an exception instance with the limit that was exceeded.
   *
   * @param pLimit the limit
   */
  public BeanContainerLimitReachedException(int pLimit)
  {
    super("The limit of this container is reached! limit: " + pLimit);
  }
}
