package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.base.IEqualsHashCodeChecker;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.exceptions.bean.*;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.types.*;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link IBean}.
 * Some methods of this interface do not have to be tested, because they are covered by other tests.
 * The test is based on the basic implementing class {@link OJBean}. So some tests include implementation details of the base class as well.
 *
 * @author Simon Danner, 28.07.2018
 */
class BeanTest
{
  private static final String VALUE = "value";
  private static final String PRIVATE_VALUE = "privateValue";

  private SomeBean bean;

  @BeforeEach
  public void init()
  {
    bean = new SomeBean();
  }

  @Test
  public void testGetValueNotExistingField()
  {
    final TextField nonExistingField = BeanFieldFactory.createField(TextField.class, "test", Collections.emptyList());
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.getValue(nonExistingField));
  }

  @Test
  public void testGetValuePrivateField()
  {
    assertThrows(BeanIllegalAccessException.class, () -> bean.getValue(SomeBean.somePrivateField));
  }

  @Test
  public void testGetValueSuccess()
  {
    assertEquals(VALUE, bean.getValue(SomeBean.specialTextField));
  }

  @Test
  public void testGetPrivateValueSuccess()
  {
    assertEquals(PRIVATE_VALUE, bean.getSomePrivateValue());
  }

  @Test
  public void testGetValueOrDefault()
  {
    bean.setValue(SomeBean.specialTextField, SpecialTextField.INITIAL_VALUE);
    assertEquals(SpecialTextField.DEFAULT_VALUE, bean.getValueOrDefault(SomeBean.specialTextField));
  }

  @Test
  public void testGetNullValueForbidden()
  {
    bean.setEncapsulatedDataSource(new _NullReturningBeanDataSource());
    assertThrows(NullValueForbiddenException.class, () -> bean.getValue(SomeBean.specialTextField)); //Field type annotated
    assertThrows(NullValueForbiddenException.class, () -> bean.getValue(SomeBean.numberField)); //Number field type annotated naturally
    assertThrows(NullValueForbiddenException.class, () -> bean.getPrivateValue(SomeBean.somePrivateField)); //Field instance annotated
  }

  @Test
  public void testSetValueNotExistingField()
  {
    final TextField nonExistingField = BeanFieldFactory.createField(TextField.class, "test", Collections.emptyList());
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.setValue(nonExistingField, "test"));
  }

  @Test
  public void testSetValuePrivateField()
  {
    assertThrows(BeanIllegalAccessException.class, () -> bean.setValue(SomeBean.somePrivateField, "test"));
  }

  @Test
  public void testSetNullValueForbidden()
  {
    assertThrows(NullValueForbiddenException.class, () -> bean.setValue(SomeBean.specialTextField, null)); //Field type annotated
    assertThrows(NullValueForbiddenException.class, () -> bean.setValue(SomeBean.numberField, null)); //Number field type annotated naturally
    assertThrows(NullValueForbiddenException.class, () -> bean.setPrivateValue(SomeBean.somePrivateField, null)); //Field instance annotated
  }

  @Test
  public void testClear()
  {
    bean.clear();
    bean.stream().forEach(pTuple -> assertEquals(pTuple.getField().getInitialValue(), pTuple.getValue()));
  }

  @Test
  public void testHasFieldPrivate()
  {
    assertThrows(BeanIllegalAccessException.class, () -> bean.hasField(SomeBean.somePrivateField));
  }

  @Test
  public void testHasFieldSuccess()
  {
    assertTrue(bean.hasField(SomeBean.specialTextField));
  }

  @Test
  public void testFieldCount()
  {
    assertEquals(3, bean.getFieldCount());
  }

  @Test
  public void testFieldIndexNotExistingField()
  {
    final TextField nonExistingField = BeanFieldFactory.createField(TextField.class, "test", Collections.emptyList());
    assertEquals(-1, bean.getFieldIndex(nonExistingField));
  }

  @Test
  public void testFieldIndexPrivateField()
  {
    assertThrows(BeanIllegalAccessException.class, () -> bean.getFieldIndex(SomeBean.somePrivateField));
  }

  @Test
  public void testFieldIndex()
  {
    assertEquals(0, bean.getFieldIndex(SomeBean.specialTextField));
    assertEquals(1, bean.getFieldIndex(SomeBean.numberField));
  }

  @Test
  public void testGetIdentifiers()
  {
    final Set<FieldValueTuple<?>> identifiers = bean.getIdentifiers();
    assertEquals(2, identifiers.size());
    final Iterator<FieldValueTuple<?>> it = identifiers.iterator();
    assertSame(SomeBean.specialTextField, it.next().getField());
    assertSame(SomeBean.numberField, it.next().getField());
  }

  @Test
  public void testEqualsAndHashCode()
  {
    final SomeBean anotherBean = new SomeBean();
    final IEqualsHashCodeChecker equalsHashCodeChecker = IEqualsHashCodeChecker.create(bean, anotherBean);
    equalsHashCodeChecker.makeAssertion(true);
    anotherBean.setPrivateValue(SomeBean.somePrivateField, "differentValue"); //This should not affect the behaviour
    equalsHashCodeChecker.makeAssertion(true);
    anotherBean.setValue(SomeBean.numberField, 111);
    equalsHashCodeChecker.makeAssertion(false);
  }

  @Test
  public void testFindFieldByNameFail()
  {
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.getFieldByName("notExisting"));
  }

  @Test
  public void testFindFieldByNameSuccess()
  {
    final IField<?> field = bean.getFieldByName("specialTextField");
    assertSame(SomeBean.specialTextField, field);
  }

  @Test
  public void testResolveDeepBean()
  {
    final IBean<?> deepBean = bean.resolveDeepBean(SomeBean.deepField, DeepBean.deeperField);
    assertSame(DeepBean.DEEPER_BEAN, deepBean);
  }

  @Test
  public void testResolveDeepValue()
  {
    final Integer deepValue = bean.resolveDeepValue(DeeperBean.deepValue, SomeBean.deepField, DeepBean.deeperField);
    assertSame(DeeperBean.DEEP_VALUE, deepValue);
  }

  /**
   * Some bean.
   */
  public static class SomeBean extends OJBean<SomeBean>
  {
    @Identifier
    public static final SpecialTextField specialTextField = OJFields.create(SomeBean.class);
    @Identifier
    public static final IntegerField numberField = OJFields.create(SomeBean.class);
    @NeverNull
    @Private
    public static final TextField somePrivateField = OJFields.create(SomeBean.class);
    public static final BeanField<DeepBean> deepField = OJFields.create(SomeBean.class);

    public SomeBean()
    {
      setValue(specialTextField, VALUE);
      setValue(numberField, 42);
      setPrivateValue(somePrivateField, PRIVATE_VALUE);
      setValue(deepField, new DeepBean());
    }

    /**
     * A private value.
     *
     * @return the value of the private value
     */
    public String getSomePrivateValue()
    {
      return getPrivateValue(somePrivateField);
    }
  }

  /**
   * A deep bean that holds a reference to another bean.
   */
  public static class DeepBean extends OJBean<DeepBean>
  {
    public static final DeeperBean DEEPER_BEAN = new DeeperBean();
    public static final BeanField<DeeperBean> deeperField = OJFields.create(DeepBean.class);

    public DeepBean()
    {
      setValue(deeperField, DEEPER_BEAN);
    }
  }

  /**
   * An even deeper bean that holds a very deep value.
   */
  public static class DeeperBean extends OJBean<DeeperBean>
  {
    public static final int DEEP_VALUE = 42;
    public static final IntegerField deepValue = OJFields.create(DeeperBean.class);

    public DeeperBean()
    {
      setValue(deepValue, DEEP_VALUE);
    }
  }

  /**
   * A text field with a special default value.
   */
  @NeverNull
  public static class SpecialTextField extends TextField
  {
    public static final String DEFAULT_VALUE = "default";
    public static final String INITIAL_VALUE = "initial";

    protected SpecialTextField(@NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
    {
      super(pName, pAnnotations);
    }

    @Override
    public String getDefaultValue()
    {
      return DEFAULT_VALUE;
    }

    @Override
    public String getInitialValue()
    {
      return INITIAL_VALUE;
    }
  }

  /**
   * A bean data source that always returns null values to test {@link NeverNull} fields.
   */
  private static class _NullReturningBeanDataSource implements IBeanDataSource
  {
    @Override
    public <VALUE> VALUE getValue(IField<VALUE> pField)
    {
      return null;
    }

    @Override
    public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue, boolean pAllowNewField)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public <VALUE> void removeField(IField<VALUE> pField)
    {
      throw new UnsupportedOperationException();
    }
  }
}