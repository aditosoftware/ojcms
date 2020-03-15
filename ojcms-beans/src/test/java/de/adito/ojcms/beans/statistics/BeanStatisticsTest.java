package de.adito.ojcms.beans.statistics;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.Statistics;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.literals.fields.types.TextField;
import io.reactivex.Observable;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static de.adito.ojcms.beans.base.reactive.ReactiveUtil.observe;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean and bean container statistics.
 * It also includes the related observers for the statistic data.
 *
 * @author Simon Danner, 16.07.2018
 */
class BeanStatisticsTest
{
  private static final int FIELD_STATISTICS_LIMIT = 10;
  private SomeBean bean;
  private IStatisticData<String> statisticData;

  @BeforeEach
  public void resetBean()
  {
    bean = new SomeBean();
    statisticData = bean.getStatisticData(SomeBean.field)
        .orElseThrow(() -> new OJInternalException("Statistics not available!"));
  }

  @Test
  public void testEntryAdditionForBean()
  {
    final String value = "value";
    assertNotNull(statisticData);
    bean.setValue(SomeBean.field, value);
    assertEquals(1, statisticData.size());
    assertEquals(value, statisticData.getChangedDataStatistics().values().iterator().next());
  }

  @Test
  public void testEntryAdditionForContainer() throws InterruptedException
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    assertTrue(container.getStatisticData().isPresent());
    final IStatisticData<Integer> statisticData = container.getStatisticData().get();
    assertNotNull(statisticData);
    //Avoid overriding the initial entry
    Thread.sleep(5); //NOSONAR
    container.addBean(bean);
    assertEquals(2, statisticData.size());
    final Iterator<Integer> it = statisticData.getChangedDataStatistics().values().iterator();
    //Initial size
    assertTrue(it.hasNext());
    assertEquals(0, (int) it.next());
    //First entry
    assertTrue(it.hasNext());
    assertEquals(1, (int) it.next());
  }

  @Test
  public void testEntryObserver()
  {
    assertNotNull(statisticData);
    final AtomicInteger index = new AtomicInteger();
    observe(statisticData, IStatisticData::observeStatistics)
        .assertCallCount(10)
        .assertOnEveryValue(pNewEntry -> assertEquals("value" + index.getAndIncrement(), pNewEntry.getValue()))
        .whenDoing(pStatistics -> IntStream.range(0, 10).forEach(pIndex -> bean.setValue(SomeBean.field, "value" + pIndex)));
  }

  @Test
  public void testIntervalStatistics() throws InterruptedException
  {
    _setupTestStatistics(10);
    final int interval = 5;
    assertNotNull(statisticData);
    final LinkedList<Long> timestamps = new LinkedList<>(statisticData.getChangedDataStatistics().keySet());
    final long totalTimestampDiff = timestamps.getLast() - timestamps.getFirst();
    final int expectedEntryCount = (int) totalTimestampDiff / interval + (totalTimestampDiff % interval == 0 ? 1 : 2);

    final Observable<Map<Long, String>> intervalStatistics = statisticData.getIntervalStatistics(interval);
    final Map<Long, String> test = intervalStatistics.blockingFirst();
    final int actualEntrySize = test.size();
    assertEquals(expectedEntryCount, actualEntrySize);

    final String newEntry = "someEntry";
    observe(statisticData, pData -> pData.getIntervalStatistics(interval))
        .assertCallCount(2)
        .assertOnEveryValue(pUpdatedMap -> {
          assertTrue(actualEntrySize < pUpdatedMap.size());
          assertEquals(newEntry, pUpdatedMap.get(new LinkedList<>(pUpdatedMap.keySet()).getLast()));
        })
        .whenDoing(pData -> {
          //wait a short time and add an entry
          try
          {
            Thread.sleep(10); //NOSONAR
          }
          catch (InterruptedException pE)
          {
            throw new OJInternalException(pE);
          }
          bean.setValue(SomeBean.field, newEntry);
        });
  }

  @Test
  public void testDataDestruction() throws InterruptedException
  {
    _setupTestStatistics(10);
    assertNotNull(statisticData);
    statisticData.destroy();
    assertEquals(0, statisticData.size());
    assertTrue(statisticData.getIntervalStatistics(5).blockingFirst().isEmpty());
  }

  @Test
  public void testCapacity() throws InterruptedException
  {
    _setupTestStatistics(15); //surpass limit
    assertEquals(FIELD_STATISTICS_LIMIT, statisticData.getChangedDataStatistics().size());
  }

  /**
   * Setups some statistic entries for the global bean.
   *
   * @param pSize the size of the entries
   */
  private void _setupTestStatistics(int pSize) throws InterruptedException
  {
    final String value = "value";
    for (int i = 0; i < pSize; i++)
    {
      bean.setValue(SomeBean.field, value + i);
      //Leave some range between the timestamps
      Thread.sleep(10); //NOSONAR
    }
  }

  /**
   * A bean type that collects statistics.
   */
  @Statistics(capacity = 100)
  public static class SomeBean extends OJBean
  {
    @Statistics(capacity = FIELD_STATISTICS_LIMIT)
    public static final TextField field = OJFields.create(SomeBean.class);
  }
}