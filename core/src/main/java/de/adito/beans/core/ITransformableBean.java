package de.adito.beans.core;

/**
 * A graphical representation of a bean.
 * The graphical component uses the bean interface and refers to the same data core as the original.
 * For further information look at {@link ITransformable}.
 *
 * @param <LOGIC>  the logical level of the transformation (field or bean)
 * @param <VISUAL> the type of the graphical components to which the logical components will be transformed to
 * @param <BEAN>   the source bean's type
 * @author Simon Danner, 07.02.2017
 */
public interface ITransformableBean<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends IBean<BEAN>, ITransformable<LOGIC, VISUAL, IBeanEncapsulated<BEAN>, BEAN>
{
  @Override
  IVisualBeanTransformator<LOGIC, VISUAL, BEAN> getTransformator();

  @Override
  default IBeanFieldActivePredicate<BEAN> getFieldActiveSupplier()
  {
    return getOriginalSource().getFieldActiveSupplier();
  }
}
