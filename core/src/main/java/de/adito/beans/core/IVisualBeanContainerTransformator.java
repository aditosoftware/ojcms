package de.adito.beans.core;

import java.util.*;
import java.util.function.Consumer;

/**
 * Beschreibt einen Transformator für Bean-Container.
 * Für genauere Informationen siehe Basis.
 *
 * @param <LOGIC>  der logische Bean-Typ (Feld oder Bean), welcher transformiert werden soll
 * @param <VISUAL> der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <BEAN>   der Typ der Quell-Bean
 * @author s.danner, 13.09.2017
 * @see IVisualTransformator
 * @see ITransformable
 */
public interface IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends IVisualTransformator<LOGIC, VISUAL, IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>>
{
  /**
   * Liefert den Container für die Listener, welche über Änderungen der Sichtbarkeitszustände der Beans informieren.
   * Wird standardmäßig nicht unterstützt, da nicht jede Komponente Sichtbarkeitszustände besitzen muss.
   *
   * @return ein Set von Consumern für eine Menge der sichtbaren Beans
   */
  default Set<Consumer<Collection<BEAN>>> getWeakVisibleListenerContainer() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }
}
