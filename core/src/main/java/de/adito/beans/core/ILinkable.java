package de.adito.beans.core;

/**
 * Describes any component that is able to register links to itself.
 * Links are based on the transformable bean components.
 * When a bean is transformed to any other component, a link will be registered by the bean.
 *
 * @author Simon Danner, 18.07.2017
 */
interface ILinkable
{
  /**
   * Determines, if a certain transformable component is linked to this component.
   *
   * @param pComponent the transformable component
   * @param <LINK>     the generic type of the transformable component
   * @return <tt>true</tt>, if a link is present
   */
  <LINK extends ITransformable> boolean isLinked(LINK pComponent);

  /**
   * Registers a weak-link of a transformable component to this component.
   *
   * @param pComponent the transformable component to link
   * @param <LINK>     the generic type of the transformable component
   */
  <LINK extends ITransformable> void registerWeakLink(LINK pComponent);
}
