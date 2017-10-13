package de.adito.beans.core;

/**
 * Definiert eine grafische Komponente, welche in einen Beans transformierbar ist und den Bean direkt abbildet.
 *
 * @param <BEAN>   der Typ des Beans, welcher transformiert werden soll
 * @param <VISUAL> der konkrete Typ dieses Interfaces
 * @author s.danner, 01.02.2017
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
