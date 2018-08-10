package de.adito.beans.core;

import de.adito.beans.core.fields.*;
import de.adito.beans.core.util.beancopy.*;
import de.adito.beans.core.util.exceptions.BeanCopyUnsupportedException;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the copy mechanism of a bean field. ({@link IField#copyValue(Object, ECopyMode, CustomFieldCopy[])}.
 * Tuples of bean field types and associated data values can be defined here.
 * For each tuple a test case will be executed, that will copy the value and test, if it is a real copy.
 *
 * @author Simon Danner, 19.04.2018
 */
public class BeanFieldCopyTest
{
  /**
   * Use this method to declare bean field types to test their copy mechanisms.
   *
   * @return a stream of field value wrappers
   */
  private static Stream<_FieldValueWrapper<?>> _fieldsToTest()
  {
    final Map<String, Integer> testMap = new HashMap<>();
    testMap.put("test1", 6);
    testMap.put("test2", 60);
    final Consumer<MapBean<String, Integer>> mapTest = pMapBean -> {
      assertEquals(2, pMapBean.stream().count());
      assertEquals(6, pMapBean.getValue(pMapBean.getFieldFromKey("test1")));
      assertEquals(60, pMapBean.getValue(pMapBean.getFieldFromKey("test2")));
      assertEquals(testMap, pMapBean);
    };

    return Stream.of(
        new _GenericFieldValueWrapper<>(BeanField.class, new SomeBean()),
        new _FieldValueWrapper<>(BooleanField.class, true),
        new _FieldValueWrapper<>(CharacterField.class, 'a'),
        new _GenericFieldValueWrapper<>(ContainerField.class, IBeanContainer.empty(SomeBean.class)),
        new _FieldValueWrapper<>(DateField.class, Instant.now()),
        new _FieldValueWrapper<>(DecimalField.class, 5.2),
        new _GenericFieldValueWrapper<>(GenericField.class, new ArrayList<>()),
        new _FieldValueWrapper<>(IntegerField.class, 1),
        new _FieldValueWrapper<>(LongField.class, 4L),
        new _FieldValueWrapper<>(ShortField.class, (short) 7),
        new _GenericFieldValueWrapper<>(MapField.class, new MapBean<>(testMap, Integer.class, false), mapTest),
        new _FieldValueWrapper<>(TextField.class, "testing"));
  }

  @ParameterizedTest
  @MethodSource("_fieldsToTest")
  public <TYPE> void testFieldCopyMechanisms(_FieldValueWrapper<TYPE> pFieldValueWrapper)
  {
    assertNotNull(pFieldValueWrapper.value);
    IField<TYPE> field = BeanFieldFactory.createField(pFieldValueWrapper.fieldType, pFieldValueWrapper.getGenericFieldType(),
                                                      "test", Collections.emptySet());
    try
    {
      TYPE copiedValue = field.copyValue(pFieldValueWrapper.value, ECopyMode.DEEP_ONLY_BEAN_FIELDS);
      assertTrue(ClassUtils.isPrimitiveOrWrapper(copiedValue.getClass()) || copiedValue != pFieldValueWrapper.value);
      pFieldValueWrapper.getOptionalTest().ifPresent(pTest -> pTest.accept((copiedValue)));
    }
    catch (BeanCopyUnsupportedException pE)
    {
      fail(pFieldValueWrapper.fieldType.getName() + " should support a copy mechanism for its value");
    }
  }

  /**
   * Some bean.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final TextField someField = BeanFieldFactory.create(SomeBean.class);
    public static final TextField anotherField = BeanFieldFactory.create(SomeBean.class);

    public SomeBean()
    {
      setValue(someField, "someValue");
      setValue(anotherField, "anotherValue");
    }
  }

  /**
   * A field value wrapper for field types with a generic data value like {@link BeanField}.
   *
   * @param <TYPE> the data type of the bean field
   */
  private static class _GenericFieldValueWrapper<TYPE> extends _FieldValueWrapper<TYPE>
  {
    /**
     * Creates a field value wrapper.
     *
     * @param pFieldType the field's type
     * @param pValue     the data value for the field
     */
    public _GenericFieldValueWrapper(Class<? extends IField> pFieldType, TYPE pValue)
    {
      this(pFieldType, pValue, null);
    }

    /**
     * Creates a field value wrapper.
     * Additionally a predicate may be defined to test the copied value afterwards.
     *
     * @param pFieldType    the field's type
     * @param pValue        the data value for the field
     * @param pOptionalTest an optional test for the copied value afterwards
     */
    public _GenericFieldValueWrapper(Class<? extends IField> pFieldType, TYPE pValue, @Nullable Consumer<TYPE> pOptionalTest)
    {
      //noinspection unchecked
      super((Class<? extends IField<TYPE>>) pFieldType, pValue, pOptionalTest);
    }

    @Nullable
    @Override
    public Class getGenericFieldType()
    {
      return value.getClass();
    }
  }

  /**
   * A field value wrapper, which defines one test case.
   * Based on a field type an instance will be created, that will copy the value of this wrapper.
   *
   * @param <TYPE> the data type of the bean field
   */
  private static class _FieldValueWrapper<TYPE>
  {
    protected final TYPE value;
    private final Class<? extends IField<TYPE>> fieldType;
    @Nullable
    private final Consumer<TYPE> optionalTest;

    /**
     * Creates a field value wrapper.
     *
     * @param pFieldType the field's type
     * @param pValue     the data value for the field
     */
    public _FieldValueWrapper(Class<? extends IField<TYPE>> pFieldType, TYPE pValue)
    {
      this(pFieldType, pValue, null);
    }

    /**
     * Creates a field value wrapper.
     * Additionally a predicate may be defined to test the copied value afterwards.
     *
     * @param pFieldType    the field's type
     * @param pValue        the data value for the field
     * @param pOptionalTest an optional test for the copied value afterwards
     */
    public _FieldValueWrapper(Class<? extends IField<TYPE>> pFieldType, TYPE pValue, @Nullable Consumer<TYPE> pOptionalTest)
    {
      fieldType = pFieldType;
      value = pValue;
      optionalTest = pOptionalTest;
    }

    /**
     * An optional generic type of the field.
     * May be null, if there's no generic type
     *
     * @return an optional generic field type
     */
    @Nullable
    public Class getGenericFieldType()
    {
      return null;
    }

    /**
     * The optional test, that allows to define a test after the copy-process.
     *
     * @return an optional test
     */
    public Optional<Consumer<TYPE>> getOptionalTest()
    {
      return Optional.ofNullable(optionalTest);
    }
  }
}
