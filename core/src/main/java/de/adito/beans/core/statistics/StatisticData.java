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
  private final long intervall; //in ms
  private final int maxEntrySize;
  private final _LimitedMap statistics;
  private final List<IStatisticsListener<TYPE>> listeners = new ArrayList<>();

  public StatisticData(long pIntervall, long pCapacity, @Nullable TYPE pFirstValue)
  {
    intervall = pIntervall;
    maxEntrySize = (int) (pCapacity / intervall);
    statistics = new _LimitedMap(maxEntrySize);
    if (pFirstValue != null)
      addEntry(pFirstValue);
  }

  @Override
  public long getIntervall()
  {
    return intervall;
  }

  @Override
  public int getMaxEntrySize()
  {
    return maxEntrySize;
  }

  @Override
  public Map<Long, TYPE> getStatistics()
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
  public synchronized void listen(IStatisticsListener<TYPE> pListener)
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
