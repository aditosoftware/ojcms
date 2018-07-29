package de.adito.beans.core.listener;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Private;
import de.adito.beans.core.base.*;
import de.adito.beans.core.fields.*;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.function.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the bean listeners.
 *
 * @author Simon Danner, 12.07.2018
 */
class BeanListenerTest extends AbstractCallCountTest
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
  @CallCount
  public void testSimpleValueChangeListener()
  {
    final String newValue = "newValue";
    bean.listenWeak(new _CallCountListener(new IBeanChangeListener<SomeBean>()
    {
      @Override
      public <TYPE> void beanChanged(SomeBean pBean, IField<TYPE> pField, TYPE pOldValue)
      {
        assertSame(SomeBean.field1, pField);
        assertEquals(INITIAL_VALUE, pOldValue);
        assertEquals(newValue, pBean.getValue(pField));
      }
    }));
    bean.setValue(SomeBean.field1, newValue);
  }

  @Test
  @CallCount
  public void testAdditionAtTheEnd()
  {
    final DecimalField fieldToAdd = new DecimalField("fieldX", Collections.emptySet());
    bean.listenWeak(new _CallCountListener((pBean, pField) -> {
      assertSame(fieldToAdd, pField);
      assertEquals(2, pBean.getFieldIndex(pField));
    }));
    bean.addField(fieldToAdd);
  }

  @Test
  @CallCount
  public void testAdditionAtACertainIndex()
  {
    final DecimalField fieldToAdd = new DecimalField("fieldX", Collections.emptySet());
    bean.listenWeak(new _CallCountListener((pBean, pField) -> {
      assertSame(fieldToAdd, pField);
      assertEquals(0, pBean.getFieldIndex(pField));
    }));
    bean.addField(fieldToAdd, 0);
  }

  @Test
  @CallCount
  public void testCreationAndAddition()
  {
    final String fieldName = "fieldName";
    bean.listenWeak(new _CallCountListener(((pBean, pField) -> {
      assertEquals(fieldName, pField.getName());
      assertEquals(String.class, pField.getType());
      assertTrue(pField.hasAnnotation(Private.class));
    })));
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
  @CallCount
  public void testFieldRemoval()
  {
    _testRemoval(pBean -> pBean.removeField(SomeBean.field1));
  }

  @Test
  @CallCount
  public void testFieldRemovalByName()
  {
    _testRemoval(pBean -> pBean.removeFieldByName(SomeBean.field1.getName()));
  }

  @Test
  @CallCount(expectedCallCount = 2)
  public void testRemoveFieldIfMultiple()
  {
    _testRemoval(pBean -> pBean.removeFieldIf(pField -> false));
  }

  @Test
  @CallCount(expectedCallCount = 2)
  public void testMultipleListeners()
  {
    bean.listenWeak(new _CallCountListener());
    bean.listenWeak(new _CallCountListener());
    bean.setValue(SomeBean.field1, "changed");
  }

  @Test
  public void testDuplicateFieldFails()
  {
    IntegerField addedField = bean.addField(IntegerField.class, "testField", Collections.emptySet());
    assertThrows(RuntimeException.class, () -> bean.addField(addedField));
  }

  @RepeatedTest(10)
  @CallCount(expectedCallCount = 0)
  public void testWeakness()
  {
    bean.listenWeak(new _CallCountListener());
    System.gc();
    bean.setValue(SomeBean.field1, "changed");
  }

  @Test
  @CallCount
  public void testSingleContainerAddition()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    container.listenWeak(new _CallCountContainerListener(true, pBean -> assertSame(bean, pBean)));
    container.addBean(bean);
  }

  @Test
  @CallCount(expectedCallCount = 2)
  public void testSingleContainerRemoval()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    container.listenWeak(new _CallCountContainerListener(false, pBean -> assertSame(bean, pBean)));
    container.addBean(bean);
    container.removeBean(bean);
  }

  @Test
  @CallCount(expectedCallCount = 10)
  public void testMultipleContainerAddition()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    container.listenWeak(new _CallCountContainerListener(false, pBean -> assertSame(bean, pBean)));
    IntStream.range(0, 10).forEach(pIndex -> container.addBean(bean));
  }

  @Test
  @CallCount
  public void testBeanChangeWithinContainer()
  {
    final IBeanContainer<SomeBean> container = IBeanContainer.empty(SomeBean.class);
    final String newValue = "changed";
    container.addBean(bean);
    container.listenWeak(new _CallCountContainerListener(new IBeanContainerChangeListener<SomeBean>()
    {
      @Override
      public <TYPE> void beanChanged(SomeBean pBean, IField<TYPE> pField, TYPE pOldValue)
      {
        assertSame(SomeBean.field1, pField);
        assertEquals(INITIAL_VALUE, pOldValue);
        assertEquals(newValue, pBean.getValue(pField));
      }
    }));
    bean.setValue(SomeBean.field1, newValue);
  }

  /**
   * Tests the removal of a bean field.
   * It will check the old value and the deleted field.
   *
   * @param pCaller a consumer of a bean to delete a field from that bean
   */
  private void _testRemoval(Consumer<SomeBean> pCaller)
  {
    bean.listenWeak(new _CallCountListener(new IBeanChangeListener<SomeBean>()
    {
      @Override
      public <TYPE> void fieldRemoved(SomeBean pBean, IField<TYPE> pField, TYPE pOldValue)
      {
        if (pField == SomeBean.field1)
          assertEquals(INITIAL_VALUE, pOldValue);
        else if (pField == SomeBean.field2)
          assertEquals(INITIAL_NUMBER, pOldValue);
        else
          fail("unknown field");
      }
    }));
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

  /**
   * Simple listener for counting calls.
   * Calls may be proxied to an original listener.
   * This listener also checks, if the bean, that was changed/field-added/field-removed, is the same as the globally defined bean.
   */
  private class _CallCountListener implements IBeanChangeListener<SomeBean>
  {
    protected final IBeanChangeListener<SomeBean> original;

    /**
     * A simple call count listener, that just counts calls.
     */
    private _CallCountListener()
    {
      original = null;
    }

    /**
     * Adds an original listener for additions.
     *
     * @param pAddedListener an action that will be executed in case of an addition
     */
    private _CallCountListener(BiConsumer<SomeBean, IField<?>> pAddedListener)
    {
      this(new IBeanChangeListener<SomeBean>()
      {
        @Override
        public <TYPE> void fieldAdded(SomeBean pBean, IField<TYPE> pField)
        {
          pAddedListener.accept(pBean, pField);
        }
      });
    }

    /**
     * Creates a call count listener with an original listener to which the calls will be proxied.
     *
     * @param pOriginal the original listener
     */
    private _CallCountListener(@Nullable IBeanChangeListener<SomeBean> pOriginal)
    {
      original = pOriginal;
    }

    @Override
    public <TYPE> void beanChanged(SomeBean pBean, IField<TYPE> pField, TYPE pOldValue)
    {
      called();
      assertSame(bean, pBean);
      if (original != null)
        original.beanChanged(pBean, pField, pOldValue);
    }

    @Override
    public <TYPE> void fieldAdded(SomeBean pBean, IField<TYPE> pField)
    {
      called();
      assertSame(bean, pBean);
      if (original != null)
        original.fieldAdded(pBean, pField);
    }

    @Override
    public <TYPE> void fieldRemoved(SomeBean pBean, IField<TYPE> pField, TYPE pOldValue)
    {
      called();
      assertSame(bean, pBean);
      if (original != null)
        original.fieldRemoved(pBean, pField, pOldValue);
    }
  }

  /**
   * Enhanced listener for containers.
   * Calls may be proxied to an original listener.
   */
  private class _CallCountContainerListener extends _CallCountListener implements IBeanContainerChangeListener<SomeBean>
  {
    /**
     * Creates a new container call count listener.
     *
     * @param pAddition     <tt>true</tt>, if this listener should check additions
     * @param pBeanConsumer a consumer of a added/removed, that defines a action for assertions
     */
    private _CallCountContainerListener(boolean pAddition, Consumer<SomeBean> pBeanConsumer)
    {
      this(pAddition ? new IBeanContainerChangeListener<SomeBean>()
      {
        @Override
        public void beanAdded(SomeBean pBean)
        {
          pBeanConsumer.accept(pBean);
        }
      } : new IBeanContainerChangeListener<SomeBean>()
      {
        @Override
        public void beanRemoved(SomeBean pBean)
        {
          pBeanConsumer.accept(pBean);
        }
      });
    }

    /**
     * Creates a container call count listener with an original listener to which the calls will be proxied.
     *
     * @param pOriginal the original listener
     */
    private _CallCountContainerListener(@Nullable IBeanContainerChangeListener<SomeBean> pOriginal)
    {
      super(pOriginal);
    }

    @Override
    public void beanAdded(SomeBean pBean)
    {
      called();
      ((IBeanContainerChangeListener<SomeBean>) original).beanAdded(pBean);
    }

    @Override
    public void beanRemoved(SomeBean pBean)
    {
      called();
      ((IBeanContainerChangeListener<SomeBean>) original).beanRemoved(pBean);
    }
  }
}