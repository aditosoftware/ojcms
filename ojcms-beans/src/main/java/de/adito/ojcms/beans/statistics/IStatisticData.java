package de.adito.ojcms.beans.statistics;

import de.adito.ojcms.beans.reactive.events.NewStatisticEntry;
import io.reactivex.Observable;

import java.util.Map;

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
   * This map is represented as an observable because the map may update when new statistic data is added.
   * If such an addition happens the observable informs about the update.
   *
   * @param pInterval the interval of the timestamps (in ms)
   * @return an observable map that holds a timestamp as key and an associated value as value (interval based)
   */
  default Observable<Map<Long, ENTRY>> getIntervalStatistics(int pInterval)
  {
    final IntervalStatisticsMap<ENTRY> map = new IntervalStatisticsMap<>(pInterval, getChangedDataStatistics());
    return observeStatistics() //
        .map(pNewEntry -> map.newEntry(pNewEntry.getTimestamp(), pNewEntry.getValue())) //
        .startWith(map);
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
