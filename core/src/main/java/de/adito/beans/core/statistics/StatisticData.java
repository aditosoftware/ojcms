package de.adito.beans.core.statistics;

import de.adito.beans.core.reactive.events.NewStatisticEntry;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Implementation of the statistic data with an evicting map to remove entries, which exceed the limit of the data.
 *
 * @param <ENTRY> the data type of the statistic entries
 * @author Simon Danner, 14.02.2017
 */
public class StatisticData<ENTRY> implements IStatisticData<ENTRY>
{
  private final int maxEntrySize;
  private final Map<Long, ENTRY> statistics;
  private final PublishSubject<NewStatisticEntry<ENTRY>> newEntryPublisher = PublishSubject.create();

  /**
   * Creates new statistic data.
   *
   * @param pCapacity   the maximum number of entries, or -1 for no limit
   * @param pFirstValue an optional first value for the data
   */
  public StatisticData(int pCapacity, @Nullable ENTRY pFirstValue)
  {
    maxEntrySize = pCapacity;
    statistics = Collections.synchronizedMap(maxEntrySize == -1 ? new LinkedHashMap<>() : new _LimitedMap(maxEntrySize));
    if (pFirstValue != null)
      addEntry(pFirstValue);
  }

  @Override
  public int getMaxEntrySize()
  {
    return maxEntrySize;
  }

  @Override
  public Map<Long, ENTRY> getChangedDataStatistics()
  {
    return Collections.unmodifiableMap(statistics);
  }

  @Override
  public void addEntry(@NotNull ENTRY pEntry)
  {
    long timeStamp = System.currentTimeMillis();
    statistics.put(timeStamp, pEntry);
    newEntryPublisher.onNext(new NewStatisticEntry<>(this, timeStamp, pEntry));
  }

  @Override
  public void destroy()
  {
    statistics.clear();
    newEntryPublisher.onComplete();
  }

  @Override
  public Observable<NewStatisticEntry<ENTRY>> observeStatistics()
  {
    return newEntryPublisher;
  }

  /**
   * A limited evicting map that only allows a certain number of entries.
   */
  private class _LimitedMap extends LinkedHashMap<Long, ENTRY>
  {
    private final int limit;

    public _LimitedMap(int pLimit)
    {
      super(pLimit * 10 / 7, 0.7f, true); //Optimize the initial limit and the load factor regarding the limit
      limit = pLimit;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, ENTRY> pEldest)
    {
      return size() > limit;
    }
  }
}
