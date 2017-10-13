package de.adito.beans.core;

/**
 * Definiert eine grafische Komponente, welche in einen Bean-Container transformierbar ist und den Bean-Container direkt abbildet.
 *
 * @param <BEAN>   der Typ der Beans, welche in dem Container enthalten ist
 * @param <VISUAL> der konkrete Typ dieses Interfaces
 * @author s.danner, 27.01.2017
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
