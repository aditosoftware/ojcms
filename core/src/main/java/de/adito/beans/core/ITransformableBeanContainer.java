package de.adito.beans.core;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * Definiert einen IBeanContainer, welcher durch eine Transformation anhand eines anderen IBeanContainers erzeugt wird.
 *
 * @param <BEAN>   der Typ der Beans, welche im Container enthalten sind
 * @param <LOGIC>  der logische Bean-Element Typ (IField, IBean oder IBeanContainer), welches transformiert werden soll
 * @param <VISUAL> der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @author s.danner, 07.02.2017
 * @see ITransformable
 */
public interface ITransformableBeanContainer<BEAN extends IBean<BEAN>, LOGIC, VISUAL>
    extends IBeanContainer<BEAN>, ITransformable<LOGIC, VISUAL, IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>>
{
  @Override
  IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN> getTransformator();

  /**
   * Liefert einen Stream aller sichtbaren Beans.
   *
   * @return ein Stream von Beans
   */
  default Stream<BEAN> streamVisibleBeans()
  {
    return stream(); //Default: alle sichtbar
  }

  /**
   * Liefert die Anzahl aller sichtbaren Beans.
   */
  default int getVisibleBeanCount()
  {
    return (int) streamVisibleBeans().count();
  }

  /**
   * Registriert einen Listener, welcher informiert wird, wenn sich die Sichtbarkeit der Beans innerhalb des Containers verändert.
   * Standardmäßig werden die Listener beim Transformator gespeichert.
   * Wenn kein solcher existiert oder dieser das nicht unterstützt, müssen diese bei den grafischen Komponenten registriert werden.
   *
   * @param pListener die Aktion, welche ausgeführt werden soll, wenn sich etwas ändert
   * @throws UnsupportedOperationException wenn kein Transformator existiert und die grafische Komponente dies nicht unterstützt.
   */
  default void listenWeakToVisibilityChange(Consumer<Collection<BEAN>> pListener) throws UnsupportedOperationException
  {
    IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN> transformator = getTransformator();
    if (transformator == null || transformator.getWeakVisibleListenerContainer() == null)
      throw new UnsupportedOperationException();
    synchronized (transformator.getWeakVisibleListenerContainer())
    {
      transformator.getWeakVisibleListenerContainer().add(pListener);
    }
  }

  /**
   * Gibt Bescheid, dass sich die Sichtbarkeitszustände der Beans im Container verändert haben.
   *
   * @throws UnsupportedOperationException wenn kein Transformator existiert oder dieser kein Listener-Registrierung zulässt
   */
  default void fireVisibilityChange() throws UnsupportedOperationException
  {
    IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN> transformator = getTransformator();
    if (transformator == null || transformator.getWeakVisibleListenerContainer() == null)
      throw new UnsupportedOperationException();
    List<BEAN> visibleBeans = streamVisibleBeans().collect(Collectors.toList());
    synchronized (transformator.getWeakVisibleListenerContainer())
    {
      transformator.getWeakVisibleListenerContainer().forEach(pListener -> pListener.accept(visibleBeans));
    }
  }

  /**
   * Vergleicht die sichtbaren Beans vor und nach einer beliebigen Aktion und feuert eine Änderung, wenn sich die Sichtbarkeit verändert hat.
   *
   * @param pAction die beliebige Aktion
   */
  default void compareVisibilityAfterActionAndFire(Runnable pAction)
  {
    List<BEAN> beforeVisible = streamVisibleBeans().collect(Collectors.toList());
    pAction.run();
    if (!compareVisibleBeans(beforeVisible))
      fireVisibilityChange();
  }

  /**
   * Vergleicht die derzeit sichtbaren Beans mit einer beliebigen Menge von Beans und bestimmt, ob diese gleich bzw. verschieden sind.
   * Kann vor allem dazu verwendet werden, ob sich aufgrund einer bestimmten Aktion der Zustand verändert hat (vorher <-> nacher).
   * Der Vergleich geschieht auf Basis von Referenzen.
   *
   * @param pToCompare die Menge, mit welcher der aktuelle Zustand verglichen werden soll
   * @return <tt>true</tt>, wenn es keine Unterschiede gibt
   */
  default boolean compareVisibleBeans(Collection<BEAN> pToCompare)
  {
    return pToCompare.size() == getVisibleBeanCount() &&
        streamVisibleBeans()
            .noneMatch(pBean -> pToCompare.stream()
                .anyMatch(pBeanToCompare -> pBean != pBeanToCompare)); //Referenzen!
  }
}
