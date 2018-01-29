package de.adito.beans.core;

import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.statistics.*;
import de.adito.beans.core.util.BeanReflector;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Implementation of the bean container encapsulated data core.
 *
 * @param <BEAN> the type of beans contained in this data.
 * @author Simon Danner, 25.01.2017
 */
class BeanListEncapsulated<BEAN extends IBean<BEAN>> extends ArrayList<BEAN> implements IBeanContainerEncapsulated<BEAN>
{
  private final Class<BEAN> beanType;
  private _LimitInfo limitInfo = null;
  private final IStatisticData<Integer> statisticsData;
  private final BeanBaseData<BEAN, IBeanContainerChangeListener<BEAN>> baseData = new BeanBaseData<>();

  /**
   * Creates the encapsulated core based on a collection of existing beans.
   *
   * @param pBeanType   the bean's type
   * @param pCollection the collection of beans
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
   * Creates the statistic data for this encapsulated core.
   * This data is an amount of timestamps with an associated number,
   * which stands for the amount of beans in this container at the timestamp.
   *
   * @param pBeanType    the type of beans in this data
   * @param pInitialSize the initial entry / the initial size of this container
   * @return the statistic data for this encapsulated core
   */
  @Nullable
  private IStatisticData<Integer> _createStatisticData(Class<BEAN> pBeanType, int pInitialSize)
  {
    Statistics statistics = BeanReflector.getContainerStatisticAnnotation(pBeanType);
    return statistics != null ? new StatisticData<>(statistics.intervall(), statistics.capacity(), pInitialSize) : null;
  }

  /**
   * Ensures the possibly set limit of this container and removes some beans if they should be evicted.
   * (For performance optimization the limit may be checked before the super-call, when using a non-evicting container)
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
   * Information about the limit of this container core.
   * Contains the limit itself and the information if old entries should be evicted.
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
