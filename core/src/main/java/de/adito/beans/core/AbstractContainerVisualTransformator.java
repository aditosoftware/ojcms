package de.adito.beans.core;

/**
 * Abstrakte Grundlage für einen Visual-Bean-Container-Transformator.
 *
 * @param <BEAN>   der Typ der Beans, welche im Container enthalten sind
 * @param <LOGIC>  der logische Bean-Element Typ (IField, IBean oder IBeanContainer), welcher transformiert werden soll
 * @param <VISUAL> der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @author s.danner, 07.02.2017
 */
public abstract class AbstractContainerVisualTransformator<BEAN extends IBean<BEAN>, LOGIC, VISUAL>
    extends AbstractVisualTransformator<LOGIC, VISUAL, IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>>
    implements IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN>
{
}
