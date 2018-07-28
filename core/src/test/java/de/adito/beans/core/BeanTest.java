package de.adito.beans.core;

import de.adito.beans.core.annotations.*;
import de.adito.beans.core.fields.*;
import de.adito.beans.core.util.exceptions.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link IBean}.
 * Some methods of this interface do not have to be tested, because they are covered by other tests.
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
    assertEquals(VALUE, bean.getValue(SomeBean.someField));
  }

  @Test
  public void testGetPrivateValueSuccess()
  {
    assertEquals(PRIVATE_VALUE, bean.getSomePrivateValue());
  }

  @Test
  public void testGetValueOrDefault()
  {
    bean.setValue(SomeBean.someField, null);
    bean.setValue(SomeBean.numberField, null);
    assertNull(bean.getValueOrDefault(SomeBean.someField));
    assertNull(bean.getValue(SomeBean.numberField));
    assertEquals(0, (int) bean.getValueOrDefault(SomeBean.numberField));
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
  public void testClear()
  {
    bean.clear();
    bean.stream()
        .map(FieldTuple::getValue)
        .forEach(Assertions::assertNull);
  }

  @Test
  public void testHasFieldPrivate()
  {
    assertThrows(BeanIllegalAccessException.class, () -> bean.hasField(SomeBean.somePrivateField));
  }

  @Test
  public void testHasFieldSuccess()
  {
    assertTrue(bean.hasField(SomeBean.someField));
  }

  @Test
  public void testFieldCount()
  {
    assertEquals(2, bean.getFieldCount());
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
    assertEquals(0, bean.getFieldIndex(SomeBean.someField));
    assertEquals(1, bean.getFieldIndex(SomeBean.numberField));
  }

  @Test
  public void testGetIdentifiers()
  {
    final Set<FieldTuple<?>> identifiers = bean.getIdentifiers();
    assertEquals(1, identifiers.size());
    assertSame(SomeBean.numberField, identifiers.iterator().next().getField());
  }

  @Test
  public void testFindFieldByNameFail()
  {
    final Optional<IField<?>> result = bean.findFieldByName("notExisting");
    assertFalse(result.isPresent());
  }

  @Test
  public void testFindFieldByNameSuccess()
  {
    final Optional<IField<?>> result = bean.findFieldByName("someField");
    assertTrue(result.isPresent());
    assertSame(SomeBean.someField, result.get());
  }

  /**
   * Some bean.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final TextField someField = BeanFieldFactory.create(SomeBean.class);
    @Identifier
    public static final IntegerField numberField = BeanFieldFactory.create(SomeBean.class);
    @Private
    public static final TextField somePrivateField = BeanFieldFactory.create(SomeBean.class);

    public SomeBean()
    {
      setValue(someField, VALUE);
      setValue(numberField, 42);
      setPrivateValue(somePrivateField, PRIVATE_VALUE);
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
}