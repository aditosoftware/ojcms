package de.adito.beans.core;

/**
 * An extension for a component that holds a bean data core.
 *
 * @param <ENCAPSULATED> the encapsulated data core's type
 * @author Simon Danner, 20.01.2017
 * @see IEncapsulated
 */
interface IEncapsulatedHolder<ENCAPSULATED extends IEncapsulated>
{
  /**
   * The encapsulated data core of a bean element.
   * This method may be used as 'virtual' field.
   * Examples could be found in {@link IBean} or {@link IBeanContainer}
   */
  ENCAPSULATED getEncapsulated();
}
