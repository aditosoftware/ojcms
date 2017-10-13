package de.adito.beans.core.statistics;

import de.adito.beans.core.listener.IStatisticsListener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Beschreibt die statistischen Daten eines Bean-Feldes bzw. eines Bean-Containers.
 *
 * @param <TYPE> der Typ der Einträge der Daten
 * @author s.danner, 14.02.2017
 */
public interface IStatisticData<TYPE>
{
  /**
   * Liefert das Intervall, in dem ein neuer Eintrag erfasst wird.
   */
  long getIntervall();

  /**
   * Liefert die maximale Anzahl der Einträge dieser Daten.
   */
  int getMaxEntrySize();

  /**
   * Liefert die eigentlichen Daten zur Statistik als Map.
   *
   * @return eine Map, welche als Schlüssel den Zeitpunkt und als Wert den Eintrag zu diesem Zeitpunkt besitzt
   */
  Map<Long, TYPE> getStatistics();

  /**
   * Fügt einen neuen statistischen Eintrag hinzu.
   *
   * @param pEntry der neue Eintrag
   */
  void addEntry(@NotNull TYPE pEntry);

  /**
   * Fügt einen neuen Listener für diese Daten hinzu.
   *
   * @param pListener der neue Listener, welcher informiert, wenn ein neuer Eintrag hinzugefügt wurde
   */
  void listen(IStatisticsListener<TYPE> pListener);

  /**
   * Entfernt einen Listener für diese Daten.
   *
   * @param pListener der Listener, welcher entfernt werden soll
   */
  void unlisten(IStatisticsListener<TYPE> pListener);

  /**
   * Löscht diese Daten.
   */
  void destroy();
}
