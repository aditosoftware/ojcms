package de.adito.beans.core;

/**
 * Beschreibt einen Transformator für Beans.
 * Für genauere Informationen siehe Basis.
 *
 * @param <LOGIC>  der logische Bean-Typ (Feld oder Bean), welcher transformiert werden soll
 * @param <VISUAL> der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <BEAN>   der Typ der Quell-Bean
 * @author s.danner, 13.09.2017
 * @see IVisualTransformator
 * @see ITransformable
 */
public interface IVisualBeanTransformator<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends IVisualTransformator<LOGIC, VISUAL, IBeanEncapsulated<BEAN>, BEAN>
{
}
