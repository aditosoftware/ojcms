package de.adito.beans.core;

import de.adito.beans.core.annotations.internal.RequiresEncapsulatedAccess;

/**
 * An extension for a component that holds a bean data core.
 *
 * @param <ENCAPSULATED> the encapsulated data core's type
 * @author Simon Danner, 20.01.2017
 * @see IEncapsulated
 */
@RequiresEncapsulatedAccess
interface IEncapsulatedHolder<ENCAPSULATED extends IEncapsulated>
{
  /**
   * The encapsulated data core of a bean element.
   * This method may be used as 'virtual' field.
   * Examples could be found in {@link IBean} or {@link IBeanContainer}
   *
   * @return the encapsulated data core of this component
   */
  ENCAPSULATED getEncapsulated();
}
