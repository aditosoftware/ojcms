package de.adito.beans.core.listener;

import de.adito.beans.core.IBean;

/**
 * Erweitert den Bean-Change-Listener für Bean-Container.
 * Zusätzlich informiert dieser Listener über das Hinzufügen und Entfernen in Bean-Containern.
 * Hier default Methoden für Adapter-Funktionalität.
 *
 * @param <BEAN> der Typ der Bean, zu welchem dieser Listener registriert werden soll
 * @author s.danner, 23.08.2016
 */
public interface IBeanContainerChangeListener<BEAN extends IBean<BEAN>> extends IBeanChangeListener<BEAN>
{
  /**
   * Ein Bean wurde dem Container hinzugefügt.
   *
   * @param pBean der neue Bean
   */
  default void beanAdded(BEAN pBean)
  {
  }

  /**
   * Ein Bean wurde dem Container entfernt.
   *
   * @param pBean der entfernte Bean
   */
  default void beanRemoved(BEAN pBean)
  {
  }
}
