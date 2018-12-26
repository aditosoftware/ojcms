package de.adito.ojcms.beans.statistics;

import java.util.LinkedHashMap;
import java.util.function.LongFunction;
import java.util.stream.LongStream;

/**
 * Linked map implementation for interval based statistic data.
 * The entries will be added from the first change timestamp of the statistics until the last for every interval.
 * For further changes a observer will add interval based entries until the timestamp of the new change.
 *
 * @param <ENTRY> the data type of the statistic entry values
 * @author Simon Danner, 28.07.2018
 */
@SuppressWarnings("squid:S2160")
class IntervalStatisticsMap<ENTRY> extends LinkedHashMap<Long, ENTRY>
{
  private final transient int interval;
  private transient LongFunction<ENTRY> valueResolver;
  private transient long firstTimestamp;
  private transient long lastTimestamp;

  /**
   * Creates a new interval based statistic map.
   * Adds the initial entries and registers an observer for new statistic entries to update automatically.
   *
   * @param pInterval       the interval for the statistics
   * @param pValueResolver  a function to resolve the according value for a timestamp
   * @param pFirstTimestamp the first timestamp of the value changes
   * @param pLastTimestamp  the last timestamp of the value changes
   * @param pStatisticData  the statistic data itself to register the observer
   */
  IntervalStatisticsMap(int pInterval, LongFunction<ENTRY> pValueResolver, long pFirstTimestamp, long pLastTimestamp,
                        IStatisticData<ENTRY> pStatisticData)
  {
    interval = pInterval;
    valueResolver = pValueResolver;
    firstTimestamp = pFirstTimestamp;
    lastTimestamp = pLastTimestamp;
    //Add initial entries and register observer for future changes
    _addEntries();
    pStatisticData.observeStatistics()
        .subscribe(pEvent -> {
          final ENTRY lastValue = valueResolver.apply(lastTimestamp);
          firstTimestamp = lastTimestamp + interval;
          lastTimestamp = pEvent.getTimestamp();
          //The last value before or the new value, when the time is reached
          valueResolver = pTime -> pTime < lastTimestamp ? lastValue : pEvent.getValue();
          if (lastTimestamp >= firstTimestamp)
            _addEntries();
        });
  }

  /**
   * Adds intervals from the current first timestamp until the last in the given interval.
   * The according values will be retrieved from the value resolver function.
   */
  private void _addEntries()
  {
    final long totalDiff = lastTimestamp - firstTimestamp;
    LongStream.iterate(firstTimestamp, pTimeStamp -> pTimeStamp + interval)
        //Add two entries, if the last entry surpasses the last entry of the changes
        .limit(totalDiff / interval + (totalDiff % interval == 0 ? 1 : 2))
        .boxed()
        .forEach(pTimestamp -> put(pTimestamp, valueResolver.apply(pTimestamp)));
  }

}
