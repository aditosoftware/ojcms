package de.adito.ojcms.persistence.util;

/**
 * All possible storage modes for persistent beans within a persistent container.
 *
 * @author Simon Danner, 29.05.2018
 */
public enum EStorageMode
{
  /**
   * The beans have to be added to the container manually by calling methods from {@link de.adito.ojcms.beans.IBeanContainer}.
   */
  MANUAL,

  /**
   * All newly created beans will be added to the according container automatically.
   */
  AUTOMATIC
}
