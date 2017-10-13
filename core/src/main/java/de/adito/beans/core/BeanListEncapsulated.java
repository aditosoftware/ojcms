package de.adito.beans.core;

import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.statistics.StatisticData;
import de.adito.beans.core.util.BeanReflector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implementierung des Bean-Container-Daten-Kerns.
 *
 * @param <BEAN> der Typ der Beans, welche in diesem Kern enthalten sind
 * @author s.danner, 25.01.2017
 */
class BeanListEncapsulated<BEAN extends IBean<BEAN>> extends ArrayList<BEAN> implements IBeanContainerEncapsulated<BEAN>
{
  private final Class<BEAN> beanType;
  private _LimitInfo limitInfo = null;
  private final IStatisticData<Integer> statisticsData;
  private final BeanBaseData<BEAN, IBeanContainerChangeListener<BEAN>> baseData = new BeanBaseData<>();

  /**
   * Erzeugt den Daten-Kern anhand einer Menge von Beans.
   *
   * @param pBeanType   der Typ der Beans
   * @param pCollection die Menge von Beans
   */
  public BeanListEncapsulated(Class<BEAN> pBeanType, Collection<BEAN> pCollection)
  {
    super(pCollection);
    beanType = pBeanType;
    statisticsData = _createStatisticData(pBeanType, pCollection.size());
  }

  @Override
  public Class<BEAN> getBeanType()
  {
    return beanType;
  }

  @Override
  public void setLimit(int pMaxCount, boolean pEvicting)
  {
    limitInfo = pMaxCount < 0 ? null : new _LimitInfo(pMaxCount, pEvicting);
  }

  @Override
  public IStatisticData<Integer> getStatisticData()
  {
    return statisticsData;
  }

  @Override
  public BeanBaseData<BEAN, IBeanContainerChangeListener<BEAN>> getBeanBaseData()
  {
    return baseData;
  }

  @Override
  public boolean add(BEAN pBean)
  {
    boolean result = super.add(pBean);
    _ensureLimit();
    return result;
  }

  @Override
  public void add(int pIndex, BEAN pElement)
  {
    super.add(pIndex, pElement);
    _ensureLimit();
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends BEAN> pCollection)
  {
    boolean result = super.addAll(pCollection);
    _ensureLimit();
    return result;
  }

  @Override
  public boolean addAll(int pIndex, @NotNull Collection<? extends BEAN> pCollection)
  {
    boolean result = super.addAll(pIndex, pCollection);
    _ensureLimit();
    return result;
  }

  @NotNull
  @Override
  public ListIterator<BEAN> listIterator(int index)
  {
    return super.listIterator(index);
  }

  /**
   * Erzeugt die statistischen Daten für diesen Kern. (Anzahl von Beans im Container in einem Zeitintervall)
   *
   * @param pBeanType    der Typ der Beans, welche in diesem Kern enthalten sind
   * @param pInitialSize der erste Eintrag in den statistischen Daten
   * @return die Statistik für den Container/Kern
   */
  private IStatisticData<Integer> _createStatisticData(Class<BEAN> pBeanType, int pInitialSize)
  {
    Statistics statistics = BeanReflector.getContainerStatisticAnnotation(pBeanType);
    return statistics != null ? new StatisticData<>(statistics.intervall(), statistics.capacity(), pInitialSize) : null;
  }

  /**
   * Prüft und passt das Limit der Bean-Liste an (falls ein Limit gesetzt ist).
   * (Für Performance-Optimierung könnte natürlich vorher schon geprüft werden bei Nicht-Evicting)
   */
  private void _ensureLimit()
  {
    if (limitInfo == null || size() <= limitInfo.limit)
      return;

    if (limitInfo.evicting)
      removeRange(0, size() - limitInfo.limit);
    else
      removeRange(size() - limitInfo.limit, size());
  }

  /**
   * Informationen zum Limit dieses Kern.
   * Beinhaltet das Limit selbst und eine Information, ob alte Beans entfernt werden sollen, wenn das Limit erreicht ist
   */
  private class _LimitInfo
  {
    private final int limit;
    private final boolean evicting;

    public _LimitInfo(int pLimit, boolean pEvicting)
    {
      limit = pLimit;
      evicting = pEvicting;
    }
  }
}
