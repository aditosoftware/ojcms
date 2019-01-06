package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.Private;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDuplicateException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.types.*;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

import static de.adito.ojcms.beans.base.reactive.ReactiveUtil.observe;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean observers.
 *
 * @author Simon Danner, 12.07.2018
 */
class BeanObserverTest
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
  public void testSimpleValueChangeObserver()
  {
    final String newValue = "newValue";
    observe(bean, IBean::observeValues)
        .assertCallCount(1)
        .assertOnEveryValue(pChange -> {
          assertSame(SomeBean.field1, pChange.getField());
          assertEquals(INITIAL_VALUE, pChange.getOldValue());
          assertEquals(newValue, pChange.getNewValue());
        })
        .whenDoing(pBean -> pBean.setValue(SomeBean.field1, newValue));
  }

  @Test
  public void testAdditionAtTheEnd()
  {
    _testAddition((pField, pBean) -> pBean.addField(pField), 2);
  }

  @Test
  public void testAdditionAtACertainIndex()
  {
    _testAddition((pField, pBean) -> pBean.addFieldAtIndex(pField, 0), 0);
  }

  @Test
  public void testCreationAndAddition()
  {
    final String fieldName = "fieldName";
    final Private annotation = new Private()
    {
      @Override
      public Class<? extends Annotation> annotationType()
      {
        return Private.class;
      }
    };

    observe(bean, IBean::observeFieldAdditions)
        .assertCallCount(1)
        .assertOnEveryValue(pChange -> {
          assertEquals(fieldName, pChange.getField().getName());
          assertEquals(String.class, pChange.getField().getDataType());
          assertTrue(pChange.getField().hasAnnotation(Private.class));
        })
        .whenDoing(pBean -> pBean.fieldAdder(TextField.class, fieldName, Collections.singleton(annotation)).addAtTheEnd());
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
  public void testMultipleObservers()
  {
    observe(bean, IBean::observeValues)
        .assertCallCount(1)
        .assertMultiple(observe(bean, IBean::observeValues).assertCallCount(1))
        .whenDoing(pBean -> pBean.setValue(SomeBean.field1, "changed"));
  }

  @Test
  public void testDuplicateFieldFails()
  {
    final IntegerField addedField = bean.fieldAdder(IntegerField.class, "testField", Collections.emptySet()).addAtTheEnd();
    assertThrows(BeanFieldDuplicateException.class, () -> bean.addField(addedField));
  }

  @Test
  public void testSingleContainerAddition()
  {
    observe(IBeanContainer.empty(SomeBean.class), IBeanContainer::observeAdditions)
        .assertCallCount(1)
        .assertOnEveryValue(pChange -> assertSame(bean, pChange.getBean()))
        .whenDoing(pContainer -> pContainer.addBean(bean));
  }

  @Test
  public void testSingleContainerRemoval()
  {
    observe(IBeanContainer.ofSingleBean(bean), IBeanContainer::observeRemovals)
        .assertCallCount(1)
        .assertOnEveryValue(pChange -> assertSame(bean, pChange.getBean()))
        .whenDoing(pContainer -> pContainer.removeBean(bean));
  }

  @Test
  public void testMultipleContainerAddition()
  {
    observe(IBeanContainer.empty(SomeBean.class), IBeanContainer::observeAdditions)
        .assertCallCount(10)
        .whenDoing(pContainer -> IntStream.range(0, 10).forEach(pIndex -> pContainer.addBean(bean)));
  }

  @Test
  public void testBeanChangeWithinContainer()
  {
    final String newValue = "changed";
    observe(IBeanContainer.ofSingleBean(bean), IBeanContainer::observeValues)
        .assertCallCount(1)
        .assertOnEveryValue(pChange -> {
          assertSame(SomeBean.field1, pChange.getField());
          assertEquals(INITIAL_VALUE, pChange.getOldValue());
          assertEquals(newValue, pChange.getNewValue());
        })
        .whenDoing(pContainer -> bean.setValue(SomeBean.field1, newValue));
  }

  @Test
  public void testObserversStayWhenDataSourceIsChanged()
  {
    observe(bean, IBean::observeValues)
        .assertCallCount(1)
        .whenDoing(pBean -> {
          pBean.setEncapsulatedDataSource(new IBeanDataSource()
          {
            @Override
            public <VALUE> VALUE getValue(IField<VALUE> pField)
            {
              return pField.getInitialValue();
            }

            @Override
            public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue, boolean pAllowNewField)
            {
            }

            @Override
            public <VALUE> void removeField(IField<VALUE> pField)
            {
            }
          });
          pBean.setValue(SomeBean.field1, "someValue");
        });
  }

  /**
   * Tests the addition of a bean field.
   * It will check the added field and the correct index of the field.
   *
   * @param pAdder         an action the add the field to the bean based on a created field instance and the bean to add the field to
   * @param pExpectedIndex the expected index of the added field
   */
  private void _testAddition(BiConsumer<IField<?>, SomeBean> pAdder, int pExpectedIndex)
  {
    final DecimalField fieldToAdd = BeanFieldFactory.createField(DecimalField.class, "fieldX", Collections.emptyList(), Optional.empty());
    observe(bean, IBean::observeFieldAdditions)
        .assertCallCount(1)
        .assertOnEveryValue(pChange -> {
          assertSame(fieldToAdd, pChange.getField());
          assertEquals(pExpectedIndex, pChange.getSource().getFieldIndex(pChange.getField()));
        })
        .whenDoing(pBean -> pAdder.accept(fieldToAdd, pBean));
  }

  /**
   * Tests the removal of a bean field.
   * It will check the old value and the deleted field.
   *
   * @param pRemover           a consumer of a bean to delete a field from that bean
   * @param pExpectedCallCount the expected number of onNext-calls
   */
  private void _testRemoval(Consumer<SomeBean> pRemover, int pExpectedCallCount)
  {
    observe(bean, IBean::observeFieldRemovals)
        .assertCallCount(pExpectedCallCount)
        .assertOnEveryValue(pChange -> {
          if (pChange.getField() == SomeBean.field1)
            assertEquals(INITIAL_VALUE, pChange.getFieldValue());
          else if (pChange.getField() == SomeBean.field2)
            assertEquals(INITIAL_NUMBER, pChange.getFieldValue());
          else
            fail("unknown field");
        })
        .whenDoing(pRemover);
  }

  /**
   * Some bean to add listeners to.
   * Has to be a modifiable bean to test the creation and removal events.
   */
  public static class SomeBean extends OJBean<SomeBean> implements IModifiableBean<SomeBean>
  {
    public SomeBean(String pValue1, int pValue2)
    {
      setValue(field1, pValue1);
      setValue(field2, pValue2);
    }

    public static final TextField field1 = OJFields.create(SomeBean.class);
    public static final IntegerField field2 = OJFields.create(SomeBean.class);
  }
}