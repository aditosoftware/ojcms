package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.base.IEqualsHashCodeChecker;
import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.exceptions.bean.*;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
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
  private static final String OTHER_VALUE = "otherValue";
  private static final String TEXT_FIELD_NAME = "testTextField";

  private SomeBean bean;

  @BeforeEach
  public void init()
  {
    bean = new SomeBean();
  }

  @Test
  public void testGetValueNotExistingField()
  {
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.getValue(_createTextField()));
  }

  @Test
  public void testGetValueSuccess()
  {
    assertEquals(VALUE, bean.getValue(SomeBean.specialTextField));
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
    assertThrows(NullValueForbiddenException.class, () -> bean.getValue(SomeBean.someOtherField));
    //Should also throw with @FinalNeverNull
    assertThrows(NullValueForbiddenException.class, () -> bean.getValue(SomeBean.someOtherFinalField));
  }

  @Test
  public void testSetValueNotExistingField()
  {
    final TextField nonExistingField = _createTextField();
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.setValue(nonExistingField, "test"));
  }

  @Test
  public void testSetNullValueForbidden()
  {
    assertThrows(NullValueForbiddenException.class, () -> bean.setValue(SomeBean.specialTextField, null)); //Field type annotated
    assertThrows(NullValueForbiddenException.class, () -> bean.setValue(SomeBean.numberField, null)); //Number field type annotated naturally
    assertThrows(NullValueForbiddenException.class, () -> bean.setValue(SomeBean.someOtherField, null));
    //Should also throw with @FinalNeverNull
    assertThrows(NullValueForbiddenException.class, () -> bean.setValue(SomeBean.someOtherFinalField, null));
  }

  @Test
  public void testFinalField()
  {
    assertThrows(FieldIsFinalException.class, () -> bean.setValue(SomeBean.finalNumberField, 99));

    bean.setValue(SomeBean.anotherFinalField, null); //Event setting null should lead to an exception afterwards
    assertThrows(FieldIsFinalException.class, () -> bean.setValue(SomeBean.anotherFinalField, "text"));

    //Test combined annotation as well
    assertThrows(FieldIsFinalException.class, () -> bean.setValue(SomeBean.someOtherFinalField, "change"));
  }

  @Test
  public void testClear()
  {
    bean.clear();

    assertEquals(OTHER_VALUE, bean.getValue(SomeBean.someOtherField)); //Should not have been cleared because of @NeverNull

    bean.stream()
        .filter(pTuple -> !pTuple.getField().mustNeverBeNull())
        .forEach(pTuple -> assertEquals(pTuple.getField().getInitialValue(), pTuple.getValue()));
  }

  @Test
  public void testHasFieldSuccess()
  {
    assertTrue(bean.hasField(SomeBean.specialTextField));
  }

  @Test
  public void testFieldCount()
  {
    assertEquals(7, bean.getFieldCount());
  }

  @Test
  public void testFieldIndexNotExistingField()
  {
    assertEquals(-1, bean.getFieldIndex(_createTextField()));
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
    anotherBean.setValue(SomeBean.someOtherField, "differentValue"); //This should not affect the behaviour
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
    final IBean deepBean = bean.resolveDeepBean(SomeBean.deepField, DeepBean.deeperField);
    assertSame(DeepBean.DEEPER_BEAN, deepBean);
  }

  @Test
  public void testResolveDeepValue()
  {
    final Integer deepValue = bean.resolveDeepValue(DeeperBean.deepValue, SomeBean.deepField, DeepBean.deeperField);
    assertSame(DeeperBean.DEEP_VALUE, deepValue);
  }

  @Test
  public void testPrivatelyDeclaredField()
  {
    final BeanWithPrivateField bean = new BeanWithPrivateField();

    assertEquals(BeanWithPrivateField.EXPECTED_VALUE, bean.getPrivateValue());

    assertEquals(0, bean.streamFields().count());
    assertEquals(0, bean.stream().count());

    //After a clear the value should still be set, because private fields are not affected
    bean.clear();
    assertEquals(BeanWithPrivateField.EXPECTED_VALUE, bean.getPrivateValue());
  }

  @Test
  public void testBeanInheritance()
  {
    final ConcreteBeanType bean = new ConcreteBeanType();
    bean.setValue(ConcreteBeanType.SOME_BASE_FIELD, 5);
    bean.setValue(ConcreteBeanType.SOME_SPECIAL_FIELD, 42);

    assertEquals(5, bean.getValue(ConcreteBeanType.SOME_BASE_FIELD));
    assertEquals(42, bean.getValue(ConcreteBeanType.SOME_SPECIAL_FIELD));
  }

  /**
   * Creates a new bean text field.
   *
   * @return the newly created field instance
   */
  private static TextField _createTextField()
  {
    return BeanFieldFactory.createField(TextField.class, TEXT_FIELD_NAME, false, Collections.emptySet(), null);
  }

  /**
   * Some bean.
   */
  public static class SomeBean extends OJBean
  {
    @Identifier
    public static final SpecialTextField specialTextField = OJFields.create(SomeBean.class);
    @Identifier
    public static final IntegerField numberField = OJFields.create(SomeBean.class);
    @NeverNull
    public static final TextField someOtherField = OJFields.create(SomeBean.class);
    @FinalNeverNull
    public static final TextField someOtherFinalField = OJFields.create(SomeBean.class);
    public static final BeanField<DeepBean> deepField = OJFields.create(SomeBean.class);
    @Final
    public static final IntegerField finalNumberField = OJFields.create(SomeBean.class);
    @Final
    public static final TextField anotherFinalField = OJFields.create(SomeBean.class); //No initial value

    public SomeBean()
    {
      setValue(specialTextField, VALUE);
      setValue(numberField, 42);
      setValue(someOtherField, OTHER_VALUE);
      setValue(someOtherFinalField, OTHER_VALUE);
      setValue(deepField, new DeepBean());
      setValue(finalNumberField, 5);
    }

  }

  /**
   * A deep bean that holds a reference to another bean.
   */
  public static class DeepBean extends OJBean
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
  public static class DeeperBean extends OJBean
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

    protected SpecialTextField(@NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional, boolean pIsPrivate)
    {
      super(pName, pAnnotations, pIsOptional, pIsPrivate);
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
   * A bean with one privately declared field.
   */
  public static class BeanWithPrivateField extends OJBean
  {
    static final int EXPECTED_VALUE = 5;
    private static final IntegerField privateField = OJFields.create(BeanWithPrivateField.class);

    public BeanWithPrivateField()
    {
      setValue(privateField, EXPECTED_VALUE);
    }

    public int getPrivateValue()
    {
      return getValue(privateField);
    }
  }

  /**
   * Abstract base type for a bean with one field.
   */
  public static abstract class AbstractBaseBeanType extends OJBean
  {
    public static final IntegerField SOME_BASE_FIELD = OJFields.create(AbstractBaseBeanType.class);
  }

  /**
   * Some concrete bean based on the abstract bean.
   */
  public static class ConcreteBeanType extends AbstractBaseBeanType
  {
    public static final IntegerField SOME_SPECIAL_FIELD = OJFields.create(ConcreteBeanType.class);
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