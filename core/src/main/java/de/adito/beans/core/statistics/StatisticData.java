package de.adito.beans.core.statistics;

import de.adito.beans.core.listener.IStatisticsListener;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Implementation of the statistic data with an evicting map to remove entries, which exceed the limit of the data.
 *
 * @param <TYPE> the data type of the statistic entries
 * @author Simon Danner, 14.02.2017
 */
public class StatisticData<TYPE> implements IStatisticData<TYPE>
{
  private final int maxEntrySize;
  private final Map<Long, TYPE> statistics;
  private final Set<IStatisticsListener<TYPE>> listeners = Collections.newSetFromMap(new WeakHashMap<>());

  /**
   * Creates new statistic data.
   *
   * @param pCapacity   the maximum number of entries, or -1 for no limit
   * @param pFirstValue an optional first value for the data
   */
  public StatisticData(int pCapacity, @Nullable TYPE pFirstValue)
  {
    maxEntrySize = pCapacity;
    statistics = maxEntrySize == -1 ? new LinkedHashMap<>() : new _LimitedMap(maxEntrySize);
    if (pFirstValue != null)
      addEntry(pFirstValue);
  }

  @Override
  public int getMaxEntrySize()
  {
    return maxEntrySize;
  }

  @Override
  public Map<Long, TYPE> getChangedDataStatistics()
  {
    return Collections.unmodifiableMap(statistics);
  }

  @Override
  public synchronized void addEntry(@NotNull TYPE pEntry)
  {
    long timeStamp = System.currentTimeMillis();
    statistics.put(timeStamp, pEntry);
    listeners.forEach(pListener -> pListener.entryAdded(timeStamp, pEntry));
  }

  @Override
  public synchronized void listenWeak(IStatisticsListener<TYPE> pListener)
  {
    listeners.add(pListener);
  }

  @Override
  public synchronized void unlisten(IStatisticsListener<TYPE> pListener)
  {
    listeners.remove(pListener);
  }

  @Override
  public synchronized void destroy()
  {
    statistics.clear();
    listeners.clear();
  }

  /**
   * A limited evicting map that only allows a certain number of entries.
   */
  private class _LimitedMap extends LinkedHashMap<Long, TYPE>
  {
    private final int limit;

    public _LimitedMap(int pLimit)
    {
      super(pLimit * 10 / 7, 0.7f, true); //Optimize the initial limit and the load factor regarding the limit
      limit = pLimit;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, TYPE> pEldest)
    {
      return size() > limit;
    }
  }
}
