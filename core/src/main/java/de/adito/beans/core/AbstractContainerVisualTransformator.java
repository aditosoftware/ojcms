package de.adito.beans.core;

/**
 * Abstract base class for a visual bean container transformator. Defines the generic types.
 *
 * @param <BEAN>   the type of beans in the container
 * @param <LOGIC>  the logic bean type to transform
 * @param <VISUAL> the visual counter type to which the container will be transformed
 * @author Simon Danner, 07.02.2017
 */
public abstract class AbstractContainerVisualTransformator<BEAN extends IBean<BEAN>, LOGIC, VISUAL>
    extends AbstractVisualTransformator<LOGIC, VISUAL, IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>>
    implements IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN>
{
}
