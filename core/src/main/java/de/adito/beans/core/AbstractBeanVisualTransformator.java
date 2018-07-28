package de.adito.beans.core;

/**
 * Abstract base class for a visual bean transformator.
 * Defines the generic types.
 *
 * @param <LOGIC>  the logic bean type to transform
 * @param <VISUAL> the visual counter type to which the bean component will be transformed
 * @param <BEAN>   the bean type that is the basis for this transformation
 * @author Simon Danner, 07.02.2017
 */
public abstract class AbstractBeanVisualTransformator<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends AbstractVisualTransformator<LOGIC, VISUAL, IBeanEncapsulated<BEAN>, BEAN> implements IVisualBeanTransformator<LOGIC, VISUAL, BEAN>
{
}
