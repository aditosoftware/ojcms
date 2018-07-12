package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MapField}.
 * The map transformation to a bean and vice versa will be tested here.
 *
 * @author Simon Danner, 07.07.2018
 */
class MapFieldTest
{
  private final Map<String, String> data = new LinkedHashMap<>();

  @BeforeEach
  public void fillMap()
  {
    data.clear();
    IntStream.range(0, 10).forEach(pIndex -> data.put("name" + pIndex, "value" + pIndex));
  }

  @Test
  public void testMapTransformation()
  {
    final SomeBean bean = new SomeBean();
    bean.setValue(SomeBean.mapField, SomeBean.mapField.createBeanFromMap(data, String.class));
    MapBean<String> mapBean = bean.getValue(SomeBean.mapField);
    final AtomicInteger index = new AtomicInteger();
    //Test in order and proper to bean transformation
    mapBean.stream()
        .forEach(pTuple -> {
          assertEquals("name" + index.get(), pTuple.getField().getName());
          assertEquals("value" + index.getAndIncrement(), pTuple.getValue());
        });
    //Test backwards transformation to map
    Map<String, String> backToMap = SomeBean.mapField.createMapFromBean(bean, String.class);
    Assertions.assertEquals(backToMap, data);
  }

  @Test
  public void testToBeanTransformationWithPredicate()
  {
    //Allow only odd numbers
    MapBean<String> mapBean = SomeBean.mapField.createBeanFromMap(data, String.class,
                                                                  pField -> Integer.parseInt(pField.getName().substring(4)) % 2 == 1);
    final AtomicInteger index = new AtomicInteger(1);
    mapBean.stream()
        .forEach(pTuple -> {
          assertTrue(index.get() <= 9); //Make sure it are only five tuples
          assertEquals("name" + index.get(), pTuple.getField().getName());
          assertEquals("value" + index.getAndAdd(2), pTuple.getValue());
        });
  }

  /**
   * Some bean with a map field.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final MapField<String> mapField = BeanFieldFactory.create(SomeBean.class);
  }
}