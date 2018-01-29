package de.adito.beans.core.statistics;

import de.adito.beans.core.listener.IStatisticsListener;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Describes some statistic data based on cyclic collected data entries.
 * The data consists of entries that contain timestamps and associated values.
 *
 * @param <TYPE> the data type of the statistic entries
 * @author Simon Danner, 14.02.2017
 */
public interface IStatisticData<TYPE>
{
  /**
   * The intervall at which an entry will be gathered.
   */
  long getIntervall();

  /**
   * The maximum number of statistic entries.
   */
  int getMaxEntrySize();

  /**
   * The statistic data itself.
   * A collection of timestamps with an associated value for each.
   *
   * @return a map that holds a timestamp as key and an associated value as value
   */
  Map<Long, TYPE> getStatistics();

  /**
   * Adds a new entry to this data.
   *
   * @param pEntry the new entry
   */
  void addEntry(@NotNull TYPE pEntry);

  /**
   * Registers a listener for this data.
   *
   * @param pListener the listener that describes how to react to an addition of a new statistic entry
   */
  void listen(IStatisticsListener<TYPE> pListener);

  /**
   * Unregisters a listener from this data.
   *
   * @param pListener the listener to remove
   */
  void unlisten(IStatisticsListener<TYPE> pListener);

  /**
   * Deletes this statistic data.
   */
  void destroy();
}
