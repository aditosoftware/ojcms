package de.adito.beans.core.statistics;

import de.adito.beans.core.listener.IStatisticsListener;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * Statistic data of bean elements.
 * It could be the data behind bean fields or the amount of beans in a container at certain timestamps.
 *
 * @param <TYPE> the data type of the statistic entries
 * @author Simon Danner, 14.02.2017
 */
public interface IStatisticData<TYPE>
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
   * A collection of timestamps with an associated value for each change.
   * The entries are ordered by their timestamps.
   * This map is a read only view of the original changes. Future changes will be updated.
   *
   * @return a map that holds a timestamp as key and an associated value as value
   */
  Map<Long, TYPE> getChangedDataStatistics();

  /**
   * The statistic data in an interval based timestamp structure.
   * Creates a key value pair for every timestamp at a certain interval with the according data value at the specific time.
   * This creates time based entries from the first entry of changed statistic data until the very last.
   * Be aware that this could result in a large set of data and may reach memory limits, if the interval is not chosen properly.
   *
   * The entries of the resulting map will be extended, if a change of an according value happens.
   *
   * @return a map that holds a timestamp as key and an associated value as value (interval based)
   */
  default Map<Long, TYPE> getIntervalStatistics(int pInterval)
  {
    if (size() == 0)
      return Collections.emptyMap();

    final Map<Long, TYPE> changes = getChangedDataStatistics();
    final LinkedList<Long> timestamps = new LinkedList<>(changes.keySet());
    final long firstTimestamp = timestamps.getFirst();
    final long lastTimestamp = timestamps.getLast();
    //Resolves the value for a timestamp (removes all outdated timestamps from the list -> the first entry will be current timestamp)
    final Function<Long, TYPE> valueResolver = pTimestamp -> {
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
  void addEntry(@NotNull TYPE pEntry);

  /**
   * Registers a weak listener for this data.
   *
   * @param pListener the listener that describes how to react to an addition of a new statistic entry
   */
  void listenWeak(IStatisticsListener<TYPE> pListener);

  /**
   * Unregisters a listener from this data.
   *
   * @param pListener the listener to remove
   */
  void unlisten(IStatisticsListener<TYPE> pListener);

  /**
   * Deletes this statistic data and removes all listeners.
   */
  void destroy();
}
