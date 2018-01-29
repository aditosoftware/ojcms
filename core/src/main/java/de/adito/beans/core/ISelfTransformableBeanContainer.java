package de.adito.beans.core;

/**
 * A graphical component that represents a bean container directly.
 * This component will be transformed to a bean container.
 * For detailed information take a look at the base interfaces.
 *
 * @param <BEAN>   the type of the beans in the container
 * @param <VISUAL> the concrete type of this interface at runtime
 * @author Simon Danner, 27.01.2017
 * @see ISelfTransformable
 */
public interface ISelfTransformableBeanContainer<BEAN extends IBean<BEAN>, VISUAL extends ISelfTransformableBeanContainer<BEAN, VISUAL>>
    extends ISelfTransformable<IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>, VISUAL>,
    ITransformableBeanContainer<BEAN, IBeanContainer<BEAN>, VISUAL>, IVisualBeanContainerTransformator<IBeanContainer<BEAN>, VISUAL, BEAN>
{
  @Override
  default VISUAL getTransformator()
  {
    return ISelfTransformable.super.getTransformator();
  }
}
