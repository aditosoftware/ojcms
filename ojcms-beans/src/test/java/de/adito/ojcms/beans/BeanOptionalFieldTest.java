package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.OptionalField;
import de.adito.ojcms.beans.base.AbstractOnNextCallCountTest;
import de.adito.ojcms.beans.fields.types.*;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean fields marked as {@link OptionalField}.
 *
 * @author Simon Danner, 30.07.2018
 */
public class BeanOptionalFieldTest extends AbstractOnNextCallCountTest
{
  private static final String DEFAULT_STRING_VALUE = "optional";
  private SomeBean bean;

  @BeforeEach
  public void initBean()
  {
    bean = new SomeBean();
  }

  @Test
  public void testActiveState()
  {
    assertEquals(1, bean.getFieldCount());
    bean.setValue(SomeBean.optionalField1, 1);
    assertEquals(3, bean.getFieldCount());
    bean.setValue(SomeBean.normalField, "test");
    assertEquals(2, bean.getFieldCount());
  }

  @Test
  public void testStream()
  {
    assertEquals(1, bean.stream().count());
    final Optional<FieldValueTuple<?>> first = bean.stream().findFirst();
    assertTrue(first.isPresent());
    assertSame(SomeBean.normalField, first.get().getField());
  }

  @Test
  public void testFieldObservers()
  {
    justCallCheck(bean.observeFieldAdditions(), 2);
    justCallCheck(bean.observeFieldRemovals(), 1);
    bean.setValue(SomeBean.optionalField1, 1);
    bean.setValue(SomeBean.normalField, "test");
  }

  /**
   * Some bean with optional fields.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final TextField normalField = BeanFieldFactory.create(SomeBean.class);
    @OptionalField(_Condition1.class)
    public static final IntegerField optionalField1 = BeanFieldFactory.create(SomeBean.class);
    @OptionalField(_Condition2.class)
    public static final DateField optionalField2 = BeanFieldFactory.create(SomeBean.class);

    public SomeBean()
    {
      setValue(normalField, DEFAULT_STRING_VALUE);
      setValue(optionalField1, 0);
      setValue(optionalField2, LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    /**
     * A condition for an optional field.
     * The field will only be active, if the value of the int field is 0.
     */
    private static class _Condition1 implements OptionalField.IActiveCondition<SomeBean>
    {
      @Override
      public boolean test(SomeBean pSomeBean)
      {
        return pSomeBean.getValue(optionalField1) == 1;
      }
    }

    /**
     * A condition for an optional field.
     * The field will only be active, if the value of the int field is 0 and the value of the normal field contains a certain string.
     */
    private static class _Condition2 implements OptionalField.IActiveCondition<SomeBean>
    {
      @Override
      public boolean test(SomeBean pSomeBean)
      {
        return DEFAULT_STRING_VALUE.equals(pSomeBean.getValue(normalField)) && pSomeBean.getValue(optionalField1) == 1;
      }
    }
  }
}
