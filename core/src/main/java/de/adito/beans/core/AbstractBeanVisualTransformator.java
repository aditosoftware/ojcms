package de.adito.beans.core;

/**
 * Abstrakte Grundlage für einen Visual-Bean-Transformator.
 *
 * @param <LOGIC>  der logische Bean-Element-Typ (IField, IBean oder IBeanContainer), welcher transformiert werden soll
 * @param <VISUAL> der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <BEAN>   der Typ der Bean, welcher Basis für die Transformation ist
 * @author s.danner, 07.02.2017
 */
public abstract class AbstractBeanVisualTransformator<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends AbstractVisualTransformator<LOGIC, VISUAL, IBeanEncapsulated<BEAN>, BEAN> implements IVisualBeanTransformator<LOGIC, VISUAL, BEAN>
{
}
