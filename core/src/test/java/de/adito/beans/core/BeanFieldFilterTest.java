package de.adito.beans.core;

import de.adito.beans.core.fields.TextField;
import de.adito.beans.core.util.IBeanFieldPredicate;
import de.adito.beans.core.util.exceptions.BeanFieldDoesNotExistException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the field filtering mechanism for {@link IBean}.
 *
 * @author Simon Danner, 16.03.2018
 */
class BeanFieldFilterTest
{
  private static final IBeanFieldPredicate predicate = (pField, pValue) -> pField != SomeBean.someField;
  private SomeBean bean;

  @BeforeEach
  public void createFilteredBean()
  {
    bean = new SomeBean();
    bean.addFieldFilter(predicate);
  }

  @Test
  public void testGetValue()
  {
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.getValue(SomeBean.someField));
  }

  @Test
  public void testSetValue()
  {
    assertThrows(BeanFieldDoesNotExistException.class, () -> bean.setValue(SomeBean.someField, "someValue"));
  }

  @Test
  public void testHasField()
  {
    assertFalse(bean.hasField(SomeBean.someField));
  }

  @Test
  public void testFindFieldByName()
  {
    assertFalse(bean.findFieldByName("someField").isPresent());
  }

  @Test
  public void testFieldCount()
  {
    assertEquals(bean.getFieldCount(), 0);
  }

  @Test
  public void testFieldIndex()
  {
    assertEquals(-1, bean.getFieldIndex(SomeBean.someField));
  }

  @Test
  public void testFieldStreamIsEmpty()
  {
    assertEquals(bean.streamFields().count(), 0);
  }

  @Test
  public void testStreamIsEmpty()
  {
    assertEquals(bean.stream().count(), 0);
  }

  @Test
  public void testFilterRemoval()
  {
    bean.removeFieldFilter(predicate);
    assertEquals(bean.getFieldCount(), 1);
  }

  @Test
  public void testFilterClearance()
  {
    bean.clearFieldFilters();
    assertEquals(bean.getFieldCount(), 1);
  }

  /**
   * Some bean to filter one field.
   */
  public static class SomeBean extends Bean<SomeBean>
  {
    public static final TextField someField = BeanFieldFactory.create(SomeBean.class);
  }
}