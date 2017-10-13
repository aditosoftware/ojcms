package de.adito.beans.core;

/**
 * Definiert eine IBean, welche durch eine Transformation anhand einer anderen Bean erzeugt wird.
 *
 * @param <LOGIC>  der logische Bean-Element Typ (IField, IBean oder IBeanContainer), welches transformiert werden soll
 * @param <VISUAL> der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <BEAN> der eigentliche Typ der Quell-Bean, welche transformiert wird
 * @author s.danner, 07.02.2017
 * @see ITransformable
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
