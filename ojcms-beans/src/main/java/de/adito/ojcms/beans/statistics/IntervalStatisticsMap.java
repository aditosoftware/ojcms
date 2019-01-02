package de.adito.ojcms.beans.statistics;

import de.adito.ojcms.beans.exceptions.OJInternalException;

import java.util.*;
import java.util.stream.LongStream;

/**
 * Linked hash map implementation for interval based statistic data.
 * The entries will be added from the first change timestamp of the statistics until the last for every interval.
 *
 * @param <ENTRY> the data type of the statistic entry values
 * @author Simon Danner, 28.07.2018
 */
@SuppressWarnings("squid:S2160")
class IntervalStatisticsMap<ENTRY> extends LinkedHashMap<Long, ENTRY>
{
  private final transient int interval; //ms
  private transient long lastTimestamp;
  private transient ENTRY lastValue;

  /**
   * Creates a new interval based statistic map and adds the initial entries.
   *
   * @param pInterval the interval for the statistics (in ms)
   * @param pChanges  the initial changes of the statistic data
   */
  IntervalStatisticsMap(int pInterval, Map<Long, ENTRY> pChanges)
  {
    interval = pInterval;
    pChanges.forEach(this::newEntry);
  }

  /**
   * A new entry/change has been added to the statistic data.
   * Adds new entries for each interval between the last timestamp and the new one.
   *
   * @param pTimeStamp the timestamp of the new entry
   * @param pEntry     the new entry
   * @return the interval map itself to enable a pipelining mechanism
   */
  Map<Long, ENTRY> newEntry(Long pTimeStamp, ENTRY pEntry)
  {
    if (lastTimestamp > pTimeStamp)
      throw new OJInternalException("The timestamp of a new statistic entry can never be before the previous one!");

    if (isEmpty())
    {
      put(pTimeStamp, pEntry);
      lastTimestamp = pTimeStamp;
    }
    else
    {
      final long start = lastTimestamp + interval;
      final long totalDiff = pTimeStamp - start;
      final long overflow = totalDiff % interval;
      lastTimestamp = pTimeStamp + overflow;
      //Add a entry for each interval between the last timestamp and the new one
      LongStream.iterate(start, pTime -> pTime + interval)
          //Add two entries, if the last entry surpasses the last entry of the changes
          .limit(totalDiff / interval + (overflow == 0 ? 1 : 2))
          .boxed()
          .forEach(pTime -> put(pTime, pTime < pTimeStamp ? lastValue : pEntry));
    }

    lastValue = pEntry;
    return this;
  }
}
