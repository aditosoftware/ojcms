package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.OptionalField;
import de.adito.ojcms.beans.fields.types.*;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import org.junit.jupiter.api.*;

import java.time.*;
import java.util.Optional;

import static de.adito.ojcms.beans.base.reactive.ReactiveUtil.observe;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bean fields marked as {@link OptionalField}.
 *
 * @author Simon Danner, 30.07.2018
 */
public class BeanOptionalFieldTest
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
    observe(bean, IBean::observeFieldAdditions)
        .assertCallCount(2)
        .whenDoing(pBean -> pBean.setValue(SomeBean.optionalField1, 1));
    observe(bean, IBean::observeFieldRemovals)
        .assertCallCount(1)
        .whenDoing(pBean -> pBean.setValue(SomeBean.normalField, "test"));
  }

  /**
   * Some bean with optional fields.
   */
  public static class SomeBean extends OJBean<SomeBean>
  {
    public static final TextField normalField = OJFields.create(SomeBean.class);
    @OptionalField
    public static final IntegerField optionalField1 = OJFields.createOptional(SomeBean.class, (pBean, pValue) -> pValue == 1);
    @OptionalField
    public static final DateField optionalField2 = OJFields.createOptional(SomeBean.class, (pBean, pValue) -> _isFieldActive(pBean));

    public SomeBean()
    {
      setValue(normalField, DEFAULT_STRING_VALUE);
      setValue(optionalField1, 0);
      setValue(optionalField2, LocalDateTime.now().toInstant(ZoneOffset.UTC));
    }

    /**
     * Active condition for the second field.
     *
     * @param pBean the bean the field belongs to
     * @return <tt>true</tt> if the field should be active
     */
    private static boolean _isFieldActive(SomeBean pBean)
    {
      return DEFAULT_STRING_VALUE.equals(pBean.getValue(normalField)) && pBean.getValue(optionalField1) == 1;
    }
  }
}
