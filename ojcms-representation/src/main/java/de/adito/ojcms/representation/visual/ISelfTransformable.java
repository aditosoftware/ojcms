package de.adito.ojcms.representation.visual;

/**
 * A transformable graphical instance that represents the source directly.
 * In this situation the transformable instance has to be the transformator as well. (combines the interfaces)
 * Because of this, some interface methods can be implemented by default.
 * But there is also a disadvantage: The data containers and the data core reference cannot be stored at the abstract transformator base.
 * The core reference has to be stored at the instance implementing this interface itself.
 *
 * @param <SOURCE>        the type of the source that will be used for the transformation
 * @param <TRANSFORMATOR> the type of the transformator (the concrete type implementing this interface)
 * @author Simon Danner, 27.01.2017
 */
interface ISelfTransformable<SOURCE extends ILinkable, TRANSFORMATOR extends ISelfVisualTransformator<SOURCE, TRANSFORMATOR>>
    extends ITransformable<SOURCE, TRANSFORMATOR, SOURCE>, ISelfVisualTransformator<SOURCE, TRANSFORMATOR>
{
  @Override
  SOURCE getOriginalSource();

  @Override
  default TRANSFORMATOR getTransformator()
  {
    //noinspection unchecked
    return (TRANSFORMATOR) this;
  }

  @Override
  default void initTransformation(SOURCE pSourceToTransform)
  {
    //noinspection unchecked
    getTransformator().link(pSourceToTransform, (TRANSFORMATOR) this);
  }
}
