package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.statistics.IStatisticData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Definiert den Daten-Kern für einen Bean-Container.
 * Dieser wird als Liste von Beans abgebildet.
 *
 * @param <BEAN> der Typ der Beans, welche dieser Daten-Kern beinhaltet
 * @author s.danner, 25.01.2017
 */
interface IBeanContainerEncapsulated<BEAN extends IBean<BEAN>> extends IEncapsulated<BEAN, BEAN, IBeanContainerChangeListener<BEAN>>, List<BEAN>
{
  /**
   * Liefert den Typ der Beans, welche dieser Daten-Kern beinhaltet
   */
  Class<BEAN> getBeanType();

  /**
   * Legt eine maximale an Anzahl für diesen Container-Daten-Kern fest.
   *
   * @param pMaxCount die maximale Anzahl an Beans, welche in diesem Kern enthalten sein dürfen. (-1 für kein Limit)
   * @param pEvicting <tt>true</tt>, wenn die ältesten Beans entfernt werden sollen, wenn die Kapazität erreicht ist
   */
  void setLimit(int pMaxCount, boolean pEvicting);

  /**
   * Liefert die statistischen Daten dieses Daten-Kerns oder null, wenn nicht vorhanden
   */
  @Nullable
  IStatisticData<Integer> getStatisticData();

  @Override
  default Stream<BEAN> stream()
  {
    return IEncapsulated.super.stream();
  }

  @Override
  default Stream<BEAN> parallelStream()
  {
    return IEncapsulated.super.parallelStream();
  }
}
