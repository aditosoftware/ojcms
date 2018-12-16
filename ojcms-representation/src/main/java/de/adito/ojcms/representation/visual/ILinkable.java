package de.adito.ojcms.representation.visual;

/**
 * Any component that is able to register links to itself.
 * Links are based on {@link ITransformable} instances.
 * When an instance is transformed to any other component, a link will be registered by the source.
 *
 * @author Simon Danner, 18.07.2017
 */
interface ILinkable
{
  /**
   * Determines, if a certain transformable instance is linked to this instance.
   *
   * @param pComponent the transformable instance
   * @param <LINK>     the generic type of the transformable instance
   * @return <tt>true</tt>, if a link is present
   */
  <LINK extends ITransformable> boolean isLinked(LINK pComponent);

  /**
   * Registers a weak link of a transformable instance to this instance.
   *
   * @param pComponent the transformable instance to link
   * @param <LINK>     the generic type of the transformable instance
   */
  <LINK extends ITransformable> void registerWeakLink(LINK pComponent);
}
