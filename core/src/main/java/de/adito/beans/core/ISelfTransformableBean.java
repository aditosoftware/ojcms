package de.adito.beans.core;

/**
 * A graphical component that represents a bean directly.
 * This component will be transformed to a bean.
 * For detailed information take a look at the base interfaces.
 *
 * @param <BEAN>   the bean's type to transform
 * @param <VISUAL> the concrete type of this interface at runtime
 * @author Simon Danner, 01.02.2017
 * @see ISelfTransformable
 */
public interface ISelfTransformableBean<BEAN extends IBean<BEAN>, VISUAL extends ISelfTransformableBean<BEAN, VISUAL>>
    extends ISelfTransformable<IBeanEncapsulated<BEAN>, BEAN, VISUAL>, ITransformableBean<BEAN, VISUAL, BEAN>, IVisualBeanTransformator<BEAN, VISUAL, BEAN>
{
  @Override
  default VISUAL getTransformator()
  {
    return ISelfTransformable.super.getTransformator();
  }
}
