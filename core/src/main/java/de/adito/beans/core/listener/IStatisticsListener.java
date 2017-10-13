package de.adito.beans.core.listener;

/**
 * Definiert einen Listener für Statistik-Änderungen bei Bean-Statistiken.
 *
 * @param <TYPE> der Typ des Statistik-Eintrags
 * @author s.danner, 20.02.2017
 */
public interface IStatisticsListener<TYPE>
{
  /**
   * Ein neuer statistischer Eintrag wurde hinzugefügt.
   *
   * @param pTimeStamp der Zeitpunkt des Hinzufügens
   * @param pEntry     der neue Eintrag
   */
  void entryAdded(long pTimeStamp, TYPE pEntry);
}
