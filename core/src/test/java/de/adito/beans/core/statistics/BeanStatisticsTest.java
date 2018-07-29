package de.adito.beans.core.statistics;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.base.*;
import de.adito.beans.core.fields.TextField;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean and bean container statistics.
 * It also includes the related listeners for the statistic data.
 *
 * @author Simon Danner, 16.07.2018
 */
class BeanStatisticsTest extends AbstractCallCountTest
{
  private static final int FIELD_STATISTICS_LIMIT = 10;
  private SomeBean bean;

  @BeforeEach
  public void resetBean()
  {
    bean = new SomeBean();
  }

  @Test
  public void testEntryAdditionForBean()
  {
    final String value = "value";
    IStatisticData<String> statisticData = bean.getStatisticData(SomeBean.field);
    assertNotNull(statisticData);
    bean.setValue(SomeBean.field, value);
    assertEquals(1, statisticData.size());
    assertEquals(value, statisticData.getChangedDataStatistics().values().iterator().next());
  }

  @Test
  public void testEntryAdditionForContainer() throws InterruptedException
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    IStatisticData<Integer> statisticData = container.getStatisticData();
    assertNotNull(statisticData);
    Thread.sleep(5); //Avoid overriding the initial entry
    container.addBean(bean);
    assertEquals(2, statisticData.size());
    Iterator<Integer> it = statisticData.getChangedDataStatistics().values().iterator();
    //Initial size
    assertTrue(it.hasNext());
    assertEquals(0, (int) it.next());
    //First entry
    assertTrue(it.hasNext());
    assertEquals(1, (int) it.next());
  }

  @Test
  @CallCount(expectedCallCount = 10)
  public void testEntryListener()
  {
    IStatisticData<String> statisticData = bean.getStatisticData(SomeBean.field);
    assertNotNull(statisticData);
    final AtomicInteger index = new AtomicInteger();
    statisticData.listenWeak((pTimeStamp, pEntry) -> {
      called();
      assertEquals("value" + index.getAndIncrement(), pEntry);
    });
    IntStream.range(0, 10).forEach(pIndex -> bean.setValue(SomeBean.field, "value" + pIndex));
  }

  @Test
  public void testIntervalStatistics() throws InterruptedException
  {
    _setupTestStatistics(10);
    final IStatisticData<String> beanStatistics = bean.getStatisticData(SomeBean.field);
    final int interval = 5;
    assertNotNull(beanStatistics);
    LinkedList<Long> timestamps = new LinkedList<>(beanStatistics.getChangedDataStatistics().keySet());
    final long totalTimestampDiff = timestamps.getLast() - timestamps.getFirst();
    final int expectedEntryCount = (int) totalTimestampDiff / interval + (totalTimestampDiff % interval == 0 ? 1 : 2);
    final Map<Long, String> intervalStatistics = beanStatistics.getIntervalStatistics(interval);
    final int actualEntrySize = intervalStatistics.size();
    assertEquals(expectedEntryCount, actualEntrySize);
    //wait a short time and add an entry, for that a statistic entry will be added
    Thread.sleep(10);
    final String newEntry = "someEntry";
    bean.setValue(SomeBean.field, newEntry);
    assertTrue(actualEntrySize < intervalStatistics.size());
    assertEquals(newEntry, intervalStatistics.get(new LinkedList<>(intervalStatistics.keySet()).getLast()));
  }

  @Test
  public void testDataDestruction()
  {
    _setupTestStatistics(10);
    final IStatisticData<String> statisticData = bean.getStatisticData(SomeBean.field);
    assertNotNull(statisticData);
    statisticData.destroy();
    assertEquals(0, statisticData.size());
    assertEquals(0, statisticData.getIntervalStatistics(5).size());
  }

  @Test
  public void testCapacity()
  {
    _setupTestStatistics(15); //surpass limit
    final IStatisticData<String> statisticData = bean.getStatisticData(SomeBean.field);
    assertNotNull(statisticData);
    assertEquals(FIELD_STATISTICS_LIMIT, statisticData.getChangedDataStatistics().size());
  }

  /**
   * Setups some statistic entries for the global bean.
   *
   * @param pSize the size of the entries
   */
  private void _setupTestStatistics(int pSize)
  {
    final String value = "value";
    IntStream.range(0, pSize)
        .forEach(pIndex -> {
          bean.setValue(SomeBean.field, value + pIndex);
          try
          {
            Thread.sleep(10); //Leave some range between the timestamps
          }
          catch (InterruptedException pE)
          {
            throw new RuntimeException(pE);
          }
        });
  }

  /**
   * A bean type that collects statistics.
   */
  @Statistics(capacity = 100)
  public static class SomeBean extends Bean<SomeBean>
  {
    @Statistics(capacity = FIELD_STATISTICS_LIMIT)
    public static final TextField field = BeanFieldFactory.create(SomeBean.class);
  }
}