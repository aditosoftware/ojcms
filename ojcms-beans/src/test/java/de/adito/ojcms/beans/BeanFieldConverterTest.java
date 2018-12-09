package de.adito.ojcms.beans;

import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.types.DateField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the originalValue converters of bean fields.
 * More precisely the methods {@link IBean#getValueConverted(IField, Class)} and {@link IBean#setValueConverted(IField, Object)}
 * will be tested with all available converters.
 *
 * @author Simon Danner, 07.07.2018
 */
class BeanFieldConverterTest
{
  private SomeBean bean;

  /**
   * Use this method to declare tests for all available converters.
   *
   * @return a stream of type value wrappers
   */
  private static Stream<_TypeValueWrapper<?, ?>> _toTest()
  {
    final long now = System.currentTimeMillis();
    final Instant expected = Instant.ofEpochMilli(now);

    return Stream.of(
        new _TypeValueWrapper<>(SomeBean.dateField, Date.class, new Date(now), expected),
        new _TypeValueWrapper<>(SomeBean.dateField, Long.class, now, expected));
  }

  @BeforeEach
  public void init()
  {
    bean = new SomeBean();
  }

  @ParameterizedTest
  @MethodSource("_toTest")
  public <SOURCE, VALUE> void testConverters(_TypeValueWrapper<SOURCE, VALUE> pWrapper)
  {
    bean.setValueConverted(pWrapper.beanField, pWrapper.originalValue);
    VALUE convertedValue = bean.getValue(pWrapper.beanField);
    assertEquals(convertedValue, pWrapper.expectedConvertedValue);
    SOURCE originalValue = bean.getValueConverted(pWrapper.beanField, pWrapper.type);
    assertEquals(originalValue, pWrapper.originalValue);
  }

  /**
   * Some bean with fields for all converters.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final DateField dateField = BeanFieldFactory.create(SomeBean.class);
  }

  /**
   * Wrapper for a data type and a originalValue of this type.
   * Both will be applied to a bean field with a converter.
   * Also an expected converted originalValue has to be defined (according to the bean fields type)
   *
   * @param <VALUE> the generic data type
   */
  private static class _TypeValueWrapper<SOURCE, VALUE>
  {
    private final IField<VALUE> beanField;
    private final Class<SOURCE> type;
    private final SOURCE originalValue;
    private final VALUE expectedConvertedValue;

    private _TypeValueWrapper(IField<VALUE> pBeanField, Class<SOURCE> pType, SOURCE pOriginalValue, VALUE pExpectedConvertedValue)
    {
      beanField = pBeanField;
      type = pType;
      originalValue = pOriginalValue;
      expectedConvertedValue = pExpectedConvertedValue;
    }
  }
}