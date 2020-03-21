package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.base.IEqualsHashCodeChecker;
import de.adito.ojcms.beans.literals.fields.IField;
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

  @Test
  public void testWithSubTypesOfDefaultFields()
  {
    //Use a value type that is a sub type of a field's base type (BeanField in this case)
    final Map<String, SomeBean> map = new HashMap<>();
    map.put("key1", new SomeBean());
    map.put("key2", new SomeBean());
    map.put("key3", new SomeBean());
    final IMapBean<String, SomeBean> mapBean = SomeBean.mapField2.createBeanFromMap(map, SomeBean.class);

    final IField<?> firstField = mapBean.streamFields() //
        .findFirst() //
        .orElseThrow(AssertionError::new);

    assertSame(BeanField.class, firstField.getClass());
  }

  /**
   * Tests, if the field tuples of a bean fit to the map entries accordingly.
   *
   * @param pBean the bean to test
   */
  private static void _testFieldTuples(IBean pBean)
  {
    final AtomicInteger index = new AtomicInteger();
    //Test in order and proper to bean transformation
    pBean.stream() //
        .forEach(pTuple ->
        {
          assertEquals(String.valueOf(index.get()), pTuple.getField().getName());
          assertEquals("value" + index.getAndIncrement(), pTuple.getValue());
        });
  }

  /**
   * Some bean with a map field.
   */
  public static class SomeBean extends OJBean
  {
    public static final MapField<Integer, String> mapField = OJFields.create(SomeBean.class);
    public static final MapField<String, SomeBean> mapField2 = OJFields.create(SomeBean.class);
  }
}