package de.adito.ojcms.beans.reactive.events;

import de.adito.ojcms.beans.reactive.AbstractEvent;
import de.adito.ojcms.beans.statistics.IStatisticData;

/**
 * A new statistic entry has been added to some statistic data.
 *
 * @param <VALUE> the value type of the statistic entry
 * @author Simon Danner, 25.11.2018
 */
public final class NewStatisticEntry<VALUE> extends AbstractEvent<IStatisticData<VALUE>>
{
  private final long timestamp;
  private final VALUE entry;

  /**
   * Creates the statistic entry event.
   *
   * @param pSource    the source statistic data the entry has been added to
   * @param pTimestamp the timestamp of the addition
   * @param pValue     the new value
   */
  public NewStatisticEntry(IStatisticData<VALUE> pSource, long pTimestamp, VALUE pValue)
  {
    super(pSource);
    timestamp = pTimestamp;
    entry = pValue;
  }

  /**
   * The timestamp of the addition.
   *
   * @return the timestamp
   */
  public long getTimestamp()
  {
    return timestamp;
  }

  /**
   * The new value within the statistic data.
   *
   * @return the new value
   */
  public VALUE getValue()
  {
    return entry;
  }
}
