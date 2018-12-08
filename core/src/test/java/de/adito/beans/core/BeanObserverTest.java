package de.adito.beans.core;

import de.adito.beans.core.annotations.Private;
import de.adito.beans.core.base.AbstractOnNextCallCountTest;
import de.adito.beans.core.fields.types.*;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the bean observers.
 *
 * @author Simon Danner, 12.07.2018
 */
class BeanObserverTest extends AbstractOnNextCallCountTest
{
  private static final String INITIAL_VALUE = "value";
  private static final int INITIAL_NUMBER = 42;

  private SomeBean bean;

  @BeforeEach
  public void reset()
  {
    bean = new SomeBean(INITIAL_VALUE, INITIAL_NUMBER);
  }

  @Test
  public void testSimpleValueChangeListener()
  {
    final String newValue = "newValue";
    observeWithCallCheck(bean.observeValues(), 1, pChange -> {
      assertSame(SomeBean.field1, pChange.getField());
      assertEquals(INITIAL_VALUE, pChange.getOldValue());
      assertEquals(newValue, pChange.getNewValue());
    });
    bean.setValue(SomeBean.field1, newValue);
  }

  @Test
  public void testAdditionAtTheEnd()
  {
    final DecimalField fieldToAdd = new DecimalField("fieldX", Collections.emptySet());
    observeWithCallCheck(bean.observeFieldAdditions(), 1, pChange -> {
      assertSame(fieldToAdd, pChange.getField());
      assertEquals(2, pChange.getSource().getFieldIndex(pChange.getField()));
    });
    bean.addField(fieldToAdd);
  }

  @Test
  public void testAdditionAtACertainIndex()
  {
    final DecimalField fieldToAdd = new DecimalField("fieldX", Collections.emptySet());
    observeWithCallCheck(bean.observeFieldAdditions(), 1, pChange -> {
      assertSame(fieldToAdd, pChange.getField());
      assertEquals(0, pChange.getSource().getFieldIndex(pChange.getField()));
    });
    bean.addField(fieldToAdd, 0);
  }

  @Test
  public void testCreationAndAddition()
  {
    final String fieldName = "fieldName";
    observeWithCallCheck(bean.observeFieldAdditions(), 1, pChange -> {
      assertEquals(fieldName, pChange.getField().getName());
      assertEquals(String.class, pChange.getField().getDataType());
      assertTrue(pChange.getField().hasAnnotation(Private.class));
    });
    bean.addField(TextField.class, fieldName, Collections.singleton(new Private()
    {
      @Override
      public Class<? extends Annotation> annotationType()
      {
        return Private.class;
      }
    }));
  }

  @Test
  public void testFieldRemoval()
  {
    _testRemoval(pBean -> pBean.removeField(SomeBean.field1), 1);
  }

  @Test
  public void testFieldRemovalByName()
  {
    _testRemoval(pBean -> pBean.removeFieldByName(SomeBean.field1.getName()), 1);
  }

  @Test
  public void testRemoveFieldIfMultiple()
  {
    _testRemoval(pBean -> pBean.removeFieldIf(pField -> true), 2);
  }

  @Test
  public void testMultipleListeners()
  {
    justCallCheck(bean.observeValues(), 1);
    justCallCheck(bean.observeValues(), 1);
    bean.setValue(SomeBean.field1, "changed");
  }

  @Test
  public void testDuplicateFieldFails()
  {
    IntegerField addedField = bean.addField(IntegerField.class, "testField", Collections.emptySet());
    assertThrows(RuntimeException.class, () -> bean.addField(addedField));
  }

  @Test
  public void testSingleContainerAddition()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    observeWithCallCheck(container.observeAdditions(), 1, pChange -> assertSame(bean, pChange.getBean()));
    container.addBean(bean);
  }

  @Test
  public void testSingleContainerRemoval()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    container.addBean(bean);
    observeWithCallCheck(container.observeRemovals(), 1, pChange -> assertSame(bean, pChange.getBean()));
    container.removeBean(bean);
  }

  @Test
  public void testMultipleContainerAddition()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    observeWithCallCheck(container.observeAdditions(), 10, pChange -> assertSame(bean, pChange.getBean()));
    IntStream.range(0, 10).forEach(pIndex -> container.addBean(bean));
  }

  @Test
  public void testBeanChangeWithinContainer()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    final String newValue = "changed";
    container.addBean(bean);
    observeWithCallCheck(container.observeValues(), 1, pChange -> {
      assertSame(SomeBean.field1, pChange.getField());
      assertEquals(INITIAL_VALUE, pChange.getOldValue());
      assertEquals(newValue, pChange.getNewValue());
    });
    bean.setValue(SomeBean.field1, newValue);
  }

  /**
   * Tests the removal of a bean field.
   * It will check the old value and the deleted field.
   *
   * @param pCaller            a consumer of a bean to delete a field from that bean
   * @param pExpectedCallCount the expected number of onNext-calls
   */
  private void _testRemoval(Consumer<SomeBean> pCaller, int pExpectedCallCount)
  {
    observeWithCallCheck(bean.observeFieldRemovals(), pExpectedCallCount, pChange -> {
      if (pChange.getField() == SomeBean.field1)
        assertEquals(INITIAL_VALUE, pChange.getFieldValue());
      else if (pChange.getField() == SomeBean.field2)
        assertEquals(INITIAL_NUMBER, pChange.getFieldValue());
      else
        fail("unknown field");
    });
    pCaller.accept(bean);
  }

  /**
   * Some bean to add listeners to.
   * Has to be a modifiable bean to test the creation and removal events.
   */
  public static class SomeBean extends Bean<SomeBean> implements IModifiableBean<SomeBean>
  {
    public SomeBean(String pValue1, int pValue2)
    {
      setValue(field1, pValue1);
      setValue(field2, pValue2);
    }

    public static final TextField field1 = BeanFieldFactory.create(SomeBean.class);
    public static final IntegerField field2 = BeanFieldFactory.create(SomeBean.class);
  }
}