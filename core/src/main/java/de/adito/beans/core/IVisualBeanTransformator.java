package de.adito.beans.core;

/**
 * A transformator for beans. (Defines the generic types)
 * For more information take a look at the base interfaces.
 *
 * @param <LOGIC>  the logical level of the transformation (field or bean)
 * @param <VISUAL> the type of the graphical components to which the logical components will be transformed to
 * @param <BEAN>   the type of the source bean
 * @author Simon Danner, 13.09.2017
 * @see ITransformable
 */
public interface IVisualBeanTransformator<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends IVisualTransformator<LOGIC, VISUAL, IBeanEncapsulated<BEAN>, BEAN>
{
}
