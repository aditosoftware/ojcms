package de.adito.ojcms.beans.statistics;

import de.adito.ojcms.beans.reactive.events.NewStatisticEntry;
import io.reactivex.Observable;

import java.util.*;
import java.util.function.Function;

/**
 * Statistic data of bean elements.
 * It could be the data behind bean fields or the amount of beans in a container at certain timestamps.
 *
 * @param <ENTRY> the data type of the statistic entries
 * @author Simon Danner, 14.02.2017
 */
public interface IStatisticData<ENTRY>
{
  /**
   * The maximum number of statistic entries.
   *
   * @return the maximum amount of entries, or -1 if there is no limit.
   */
  int getMaxEntrySize();

  /**
   * The size of the statistic data.
   * This will be the number of entries, that represent a value change at a certain time.
   *
   * @return the size of the data
   */
  default int size()
  {
    return getChangedDataStatistics().size();
  }

  /**
   * The changes of the bean element.
   * A mapping of timestamps with an associated value for each change.
   * The entries are ordered by their timestamps.
   * This map is a read only view of the original changes.
   * Future changes will be updated automatically.
   *
   * @return a map that holds a timestamp as key and an associated value as value
   */
  Map<Long, ENTRY> getChangedDataStatistics();

  /**
   * The statistic data in an interval based timestamp structure.
   * Creates a key value pair for every timestamp at a certain interval with the according data value at the specific time.
   * This creates time based entries from the first entry of changed statistic data until the very last.
   * Be aware that this could result in a large set of data and may reach memory limits, if the interval is not chosen properly.
   *
   * The entries of the resulting map will be updated automatically, if a change of an according value happens.
   *
   * @return a map that holds a timestamp as key and an associated value as value (interval based)
   */
  default Map<Long, ENTRY> getIntervalStatistics(int pInterval)
  {
    if (size() == 0)
      return Collections.emptyMap();

    final Map<Long, ENTRY> changes = getChangedDataStatistics();
    final LinkedList<Long> timestamps = new LinkedList<>(changes.keySet());
    final long firstTimestamp = timestamps.getFirst();
    final long lastTimestamp = timestamps.getLast();
    //Resolves the value for a timestamp (removes all outdated timestamps from the list -> the first entry will be current timestamp)
    final Function<Long, ENTRY> valueResolver = pTimestamp -> {
      while (timestamps.size() > 1 && timestamps.get(1) <= pTimestamp)
        timestamps.removeFirst();
      return changes.get(timestamps.getFirst());
    };

    return new IntervalStatisticsMap<>(pInterval, valueResolver, firstTimestamp, lastTimestamp, this);
  }

  /**
   * Adds a new entry to this data.
   *
   * @param pEntry the new entry
   */
  void addEntry(ENTRY pEntry);

  /**
   * Deletes this statistic data.
   */
  void destroy();

  /**
   * Observes the statistic entries of this data.
   *
   * @return a observable which publishes new statistic entries
   */
  Observable<NewStatisticEntry<ENTRY>> observeStatistics();
}
