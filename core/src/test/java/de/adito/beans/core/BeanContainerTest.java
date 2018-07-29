package de.adito.beans.core;

import de.adito.beans.core.fields.IntegerField;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link IBeanContainer}
 *
 * @author Simon Danner, 28.07.2018
 */
class BeanContainerTest
{
  private IBeanContainer<SomeBean> container;

  @BeforeEach
  public void init()
  {
    container = IBeanContainer.empty(SomeBean.class);
  }

  @Test
  public void testBeanType()
  {
    assertSame(SomeBean.class, container.getBeanType());
  }

  @Test
  public void testAddition()
  {
    final SomeBean bean = new SomeBean();
    container.addBean(bean);
    assertEquals(1, container.size());
    assertSame(bean, container.getBean(0));
    container.addBean(bean); //Duplicate should be ok
    assertEquals(2, container.size());
  }

  @Test
  public void testAdditionAtBadIndex()
  {
    final SomeBean bean = new SomeBean();
    assertThrows(IndexOutOfBoundsException.class, () -> container.addBean(bean, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> container.addBean(bean, 1));
  }

  @Test
  public void testAdditionAtIndex()
  {
    final SomeBean bean1 = new SomeBean();
    final SomeBean bean2 = new SomeBean();
    container.addBean(bean1);
    container.addBean(bean2, 0);
    assertEquals(2, container.size());
    assertSame(bean2, container.getBean(0));
  }

  @Test
  public void testMultipleAddition()
  {
    container.addMultiple(Arrays.asList(new SomeBean(), new SomeBean(), new SomeBean()));
    assertEquals(3, container.size());
  }

  @Test
  public void testMerge()
  {
    IBeanContainer<SomeBean> container1 = IBeanContainer.ofIterableNotEmpty(Arrays.asList(new SomeBean(), new SomeBean(), new SomeBean()));
    IBeanContainer<SomeBean> container2 = IBeanContainer.ofVariableNotEmpty(new SomeBean(), new SomeBean());
    container1.merge(container2);
    assertEquals(5, container1.size());
  }

  @Test
  public void testReplacementAtBadIndex()
  {
    final SomeBean bean = new SomeBean();
    assertThrows(IndexOutOfBoundsException.class, () -> container.replaceBean(bean, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> container.replaceBean(bean, 1));
  }

  @Test
  public void testReplacement()
  {
    final SomeBean bean1 = new SomeBean();
    final SomeBean bean2 = new SomeBean();
    container.addBean(bean1);
    container.replaceBean(bean2, 0);
    assertEquals(1, container.size());
    assertSame(bean2, container.getBean(0));
  }

  @Test
  public void testRemoval()
  {
    final SomeBean bean = new SomeBean();
    container.addBean(bean);
    assertTrue(container.removeBean(bean));
    assertTrue(container.isEmpty());
    container.addBean(bean);
    assertSame(bean, container.removeBean(0));
    assertTrue(container.isEmpty());
  }

  @Test
  public void testRemoveIf()
  {
    final SomeBean bean1 = new SomeBean();
    final SomeBean bean2 = new SomeBean();
    bean1.setValue(SomeBean.someField, 0);
    bean2.setValue(SomeBean.someField, 1);
    container.addMultiple(Arrays.asList(bean1, bean2));
    container.removeBeanIf(pBean -> pBean.getValue(SomeBean.someField) == 0);
    assertEquals(1, container.size());
    assertSame(bean2, container.getBean(0));
  }

  @Test
  public void testRemoveIfAndBreak()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.ofVariableNotEmpty(new SomeBean(), new SomeBean(), new SomeBean());
    container.removeBeanIfAndBreak(pBean -> true);
    assertEquals(2, container.size());
  }

  @Test
  public void testGetBeanBadIndex()
  {
    container.addBean(new SomeBean());
    assertThrows(IndexOutOfBoundsException.class, () -> container.getBean(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> container.getBean(1));
  }

  @Test
  public void testGetBean()
  {
    final SomeBean bean = new SomeBean();
    container.addBean(bean);
    assertSame(bean, container.getBean(0));
    container.addBean(new SomeBean());
    assertSame(bean, container.getBean(0));
  }

  @Test
  public void testIndexOf()
  {
    final SomeBean bean = new SomeBean();
    container.addBean(bean);
    assertEquals(0, container.indexOf(bean));
    container.addBean(new SomeBean());
    assertEquals(0, container.indexOf(bean));
  }

  @Test
  public void testClear()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.ofVariableNotEmpty(new SomeBean(), new SomeBean(), new SomeBean());
    container.clear();
    assertTrue(container.isEmpty());
  }

  @Test
  public void testContains()
  {
    final SomeBean bean = new SomeBean();
    container.addBean(bean);
    assertTrue(container.contains(bean));
    assertFalse(container.contains(new SomeBean()));
  }

  @Test
  public void testSort()
  {
    Stream<SomeBean> stream = IntStream.range(0, 5)
        .mapToObj(SomeBean::new);
    IBeanContainer<SomeBean> container = IBeanContainer.ofStreamNotEmpty(stream);
    container.sort(Comparator.reverseOrder());
    IntStream.range(0, 5)
        .forEach(pIndex -> assertSame(4 - pIndex, container.getBean(pIndex).getValue(SomeBean.someField)));
  }

  @Test
  public void testWithLimitNotEvicting()
  {
    container.withLimit(1, false);
    container.addBean(new SomeBean());
    assertThrows(RuntimeException.class, () -> container.addBean(new SomeBean()));
  }

  @Test
  public void testWithLimitEvicting()
  {
    final SomeBean bean = new SomeBean();
    container.withLimit(1, true);
    container.addBean(new SomeBean());
    container.addBean(bean);
    assertEquals(1, container.size());
    assertSame(bean, container.getBean(0));
  }

  @Test
  public void testWithLimitExceed()
  {
    final SomeBean bean = new SomeBean();
    container.addBean(new SomeBean());
    container.addBean(bean);
    container.withLimit(1, false);
    assertEquals(1, container.size());
    assertSame(bean, container.getBean(0));
  }

  @Test
  public void testGetDistinctValues()
  {
    final SomeBean bean1 = new SomeBean(1);
    final SomeBean bean2 = new SomeBean(2);
    final SomeBean bean3 = new SomeBean(1);
    container.addMultiple(Arrays.asList(bean1, bean2, bean3));
    final Set<Integer> distinctValues = container.getDistinctValuesFromField(SomeBean.someField);
    assertEquals(2, distinctValues.size());
    assertTrue(distinctValues.contains(1));
    assertTrue(distinctValues.contains(2));
  }

  @Test
  public void testAsReadOnly()
  {
    final IBeanContainer<SomeBean> readOnly = container.asReadOnly();
    assertThrows(UnsupportedOperationException.class, () -> readOnly.addBean(new SomeBean()));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.addBean(new SomeBean(), 0));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.addMultiple(Collections.singletonList(new SomeBean())));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.addMultiple(Stream.of(new SomeBean())));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.withLimit(1, false));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.sort(Comparator.reverseOrder()));
    assertThrows(UnsupportedOperationException.class, readOnly::clear);
    assertThrows(UnsupportedOperationException.class, () -> readOnly.removeBeanIf(pBean -> true));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.removeBeanIfAndBreak(pBean -> true));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.removeBean(new SomeBean()));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.removeBean(0));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.merge(IBeanContainer.empty(SomeBean.class)));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.replaceBean(new SomeBean(), 0));
    assertThrows(UnsupportedOperationException.class, () -> readOnly.asList().clear());
  }

  /**
   * Some bean for the container.
   */
  public static class SomeBean extends Bean<SomeBean> implements Comparable<SomeBean>
  {
    public static final IntegerField someField = BeanFieldFactory.create(SomeBean.class);

    public SomeBean()
    {
    }

    public SomeBean(int pValue)
    {
      setValue(someField, pValue);
    }

    @Override
    public int compareTo(@NotNull SomeBean pBean)
    {
      return getValue(someField) - pBean.getValue(someField);
    }
  }
}