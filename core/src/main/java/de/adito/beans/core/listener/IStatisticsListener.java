package de.adito.beans.core.listener;

/**
 * A listener for changes of bean statistic data.
 *
 * @param <TYPE> the type of the statistic's entries
 * @author Simon Danner, 20.02.2017
 */
public interface IStatisticsListener<TYPE>
{
  /**
   * A new statistic entry has been added.
   *
   * @param pTimeStamp the timestamp of the addition
   * @param pEntry     the value of the new entry
   */
  void entryAdded(long pTimeStamp, TYPE pEntry);
}
