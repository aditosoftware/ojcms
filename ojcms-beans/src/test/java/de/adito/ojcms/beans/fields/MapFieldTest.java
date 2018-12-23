package de.adito.ojcms.beans.fields;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.base.IEqualsHashCodeChecker;
import de.adito.ojcms.beans.IMapBean;
import de.adito.ojcms.beans.fields.types.MapField;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MapField}.
 * The map transformation to a bean and vice versa will be tested here.
 *
 * @author Simon Danner, 07.07.2018
 */
class MapFieldTest
{
  private final Map<Integer, String> data = new LinkedHashMap<>();

  @BeforeEach
  public void fillMap()
  {
    data.clear();
    IntStream.range(0, 10).forEach(pIndex -> data.put(pIndex, "value" + pIndex));
  }

  @Test
  public void testMapTransformation()
  {
    final SomeBean bean = new SomeBean();
    bean.setValue(SomeBean.mapField, SomeBean.mapField.createBeanFromMap(data, String.class));
    _testFieldTuples(bean.getValue(SomeBean.mapField));
    //Test backwards transformation to map
    final Map<Integer, String> backToMap = SomeBean.mapField.createMapFromBean(bean);
    assertEquals(data, backToMap);
  }

  @Test
  public void testMapSize()
  {
    final SomeBean bean = new SomeBean();
    bean.setValue(SomeBean.mapField, SomeBean.mapField.createBeanFromMap(data, String.class));
    assertEquals(data.size(), bean.getValue(SomeBean.mapField).size());
  }

  @Test
  public void testIteratorRemove()
  {
    final SomeBean bean = new SomeBean();
    bean.setValue(SomeBean.mapField, SomeBean.mapField.createBeanFromMap(data, String.class));
    final Map<Integer, String> map = bean.getValue(SomeBean.mapField);
    map.entrySet().removeIf(pEntry -> pEntry.getKey() == 5);
    assertEquals(9, map.size());
    assertTrue(map.values().stream().noneMatch(pValue -> pValue.equals("value5")));
  }

  @Test
  public void testEqualsAndHashCode()
  {
    final IMapBean<Integer, String> mapBean1 = SomeBean.mapField.createBeanFromMap(data, String.class);
    final IMapBean<Integer, String> mapBean2 = SomeBean.mapField.createBeanFromMap(data, String.class);
    final IEqualsHashCodeChecker equalsHashCodeChecker = IEqualsHashCodeChecker.create(mapBean1, mapBean2);
    equalsHashCodeChecker.makeAssertion(true);
    mapBean2.put(10, "value10");
    equalsHashCodeChecker.makeAssertion(false);
    mapBean2.remove(10);
    equalsHashCodeChecker.makeAssertion(true);
    mapBean2.put(5, "differentValue");
    equalsHashCodeChecker.makeAssertion(false);
  }

  /**
   * Tests, if the field tuples of a bean fit to the map entries accordingly.
   *
   * @param pBean the bean to test
   */
  private static void _testFieldTuples(IBean<?> pBean)
  {
    final AtomicInteger index = new AtomicInteger();
    //Test in order and proper to bean transformation
    pBean.stream()
        .forEach(pTuple -> {
          assertEquals(String.valueOf(index.get()), pTuple.getField().getName());
          assertEquals("value" + index.getAndIncrement(), pTuple.getValue());
        });
  }

  /**
   * Some bean with a map field.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final MapField<Integer, String> mapField = BeanFieldFactory.create(SomeBean.class);
  }
}