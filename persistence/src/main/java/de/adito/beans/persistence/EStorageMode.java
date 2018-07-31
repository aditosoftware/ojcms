package de.adito.beans.persistence;

import de.adito.beans.core.IBean;

/**
 * All possible storage modes for persistent beans within a persistent container.
 *
 * @author Simon Danner, 29.05.2018
 */
public enum EStorageMode
{
  /**
   * The beans have to be added to the container manually by calling {@link de.adito.beans.core.IBeanContainer#addBean(IBean)}, for example.
   */
  MANUAL,

  /**
   * All newly created beans will be added to the according container automatically.
   */
  AUTOMATIC
}
