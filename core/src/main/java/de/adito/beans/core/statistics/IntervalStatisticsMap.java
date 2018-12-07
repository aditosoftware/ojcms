package de.adito.beans.core.statistics;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.LongStream;

/**
 * Linked map implementation for interval based statistic data.
 * The entries will be added from the first change timestamp of the statistics until the last for every interval.
 * For further changes a listener will add interval based entries until the timestamp of the new change.
 *
 * @author Simon Danner, 28.07.2018
 */
class IntervalStatisticsMap<TYPE> extends LinkedHashMap<Long, TYPE>
{
  private final int interval;
  private Function<Long, TYPE> valueResolver;
  private long firstTimestamp;
  private long lastTimestamp;

  /**
   * Creates a new interval based statistic map.
   * Adds the initial entries and registers a weak listener at the statistic data.
   *
   * @param pInterval       the interval for the statistics
   * @param pValueResolver  a function to resolve the according value from a timestamp
   * @param pFirstTimestamp the first timestamp of the value changes
   * @param pLastTimestamp  the last timestamp of the value changes
   * @param pStatisticData  the statistic data itself to register the listener
   */
  IntervalStatisticsMap(int pInterval, Function<Long, TYPE> pValueResolver, long pFirstTimestamp, long pLastTimestamp,
                        IStatisticData<TYPE> pStatisticData)
  {
    interval = pInterval;
    valueResolver = pValueResolver;
    firstTimestamp = pFirstTimestamp;
    lastTimestamp = pLastTimestamp;
    //Add initial entries and register observer for future changes
    _addEntries();
    pStatisticData.observeStatistics()
        .subscribe(pEvent -> {
          final TYPE lastValue = valueResolver.apply(lastTimestamp);
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
    long totalDiff = lastTimestamp - firstTimestamp;
    LongStream.iterate(firstTimestamp, pTimeStamp -> pTimeStamp + interval)
        //Add two entries, if the last entry surpasses the last entry of the changes
        .limit(totalDiff / interval + (totalDiff % interval == 0 ? 1 : 2))
        .boxed()
        .forEach(pTimestamp -> put(pTimestamp, valueResolver.apply(pTimestamp)));
  }

}
