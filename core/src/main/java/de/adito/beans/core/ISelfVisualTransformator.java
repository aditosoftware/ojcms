package de.adito.beans.core;

/**
 * An analog {@link IVisualTransformator} for {@link ISelfTransformable} components.
 * Self-transformable components represent a bean or bean container directly.
 * This interface provides a default implementation for the methods that return a graphical component for a logical component.
 * The visual component always has to be the component itself, because it is a direct representation of the logical counterpart.
 *
 * @author Simon Danner, 27.01.2017
 */
interface ISelfVisualTransformator<ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>,
    TRANSFORMATOR extends ISelfVisualTransformator<ENCAPSULATED, SOURCE, TRANSFORMATOR>>
    extends IVisualTransformator<SOURCE, TRANSFORMATOR, ENCAPSULATED, SOURCE>
{
  @Override
  default TRANSFORMATOR createVisualComponent(SOURCE pLogicComponent)
  {
    //noinspection unchecked
    return (TRANSFORMATOR) this;
  }

  @Override
  default TRANSFORMATOR createLinkedVisualComponent(SOURCE pLogicComponent)
  {
    //noinspection unchecked
    return (TRANSFORMATOR) this;
  }
}
