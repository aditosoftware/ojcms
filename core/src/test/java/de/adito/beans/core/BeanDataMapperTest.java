package de.adito.beans.core;

import de.adito.beans.core.fields.TextField;
import de.adito.beans.core.mappers.*;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the data mapping mechanism for {@link IBean}.
 *
 * @author Simon Danner, 08.04.2018
 */
class BeanDataMapperTest
{
  private static final String PREFIX = "PREFIX";

  @Test
  public void testSimpleSingleMapping()
  {
    SomeBean bean = _createBeanWithMapper((IBeanDataMapper) (pField, pValue) -> pField.newUntypedTuple(PREFIX + pValue));
    bean.stream().forEach(pFieldTuple -> assertTrue(((String) pFieldTuple.getValue()).startsWith(PREFIX)));
  }

  @Test
  public void testFlatMapping()
  {
    SomeBean bean = _createBeanWithMapper((pField, pValue) -> IntStream.range(0, 10).mapToObj(pIndex -> pField.newUntypedTuple(PREFIX + pValue)));
    bean.stream().forEach(pFieldTuple -> assertTrue(((String) pFieldTuple.getValue()).startsWith(PREFIX)));
    assertEquals(bean.stream().count(), 20); //Multiplied each tuple ten times
  }

  @Test
  public void testMapperRemoval()
  {
    IBeanDataMapper mapper = (pField, pValue) -> pField.newUntypedTuple(PREFIX + pValue);
    SomeBean bean = _createBeanWithMapper(mapper);
    assertTrue(bean.removeDataMapper(mapper));
    bean.stream().forEach(pFieldTuple -> assertFalse(((String) pFieldTuple.getValue()).startsWith(PREFIX)));
  }

  @Test
  public void testMapperClearance()
  {
    SomeBean bean = _createBeanWithMapper((IBeanDataMapper) (pField, pValue) -> pField.newUntypedTuple(PREFIX + pValue));
    bean.clearDataMappers();
    bean.stream().forEach(pFieldTuple -> assertFalse(((String) pFieldTuple.getValue()).startsWith(PREFIX)));
  }

  @Test
  public void testSingleFieldMapper()
  {
    SomeBean bean = new SomeBean();
    bean.addDataMapperForField(SomeBean.someField, (ISingleFieldDataMapper<String>) (pField, pValue) -> pField.newTuple(PREFIX + pValue));
    bean.stream().forEach(pFieldTuple -> {
      String value = (String) pFieldTuple.getValue();
      if (pFieldTuple.getField() == SomeBean.someField)
        assertTrue(value.startsWith(PREFIX));
      else
        assertFalse(value.startsWith(PREFIX));
    });
  }

  @Test
  public void testSingleFieldFlatMapper()
  {
    SomeBean bean = new SomeBean();
    bean.addDataMapperForField(SomeBean.someField, (pField, pValue) -> IntStream.range(0, 10)
        .mapToObj(pIndex -> pField.newTuple(PREFIX + pValue)));
    assertEquals(bean.stream().count(), 11);
  }

  @Test
  public void testMultipleMappers()
  {
    IBeanDataMapper mapper = (pField, pValue) -> pField.newUntypedTuple(PREFIX + pValue);
    SomeBean bean = _createBeanWithMapper(mapper);
    bean.addDataMapper(mapper);
    bean.stream().forEach(pFieldTuple -> assertTrue(((String) pFieldTuple.getValue()).startsWith(PREFIX + PREFIX)));
  }

  /**
   * Creates a test bean with a mapper.
   *
   * @param pMapper the data mapper to add
   * @return the created bean
   */
  private SomeBean _createBeanWithMapper(IBeanFlatDataMapper pMapper)
  {
    SomeBean bean = new SomeBean();
    bean.addDataMapper(pMapper);
    return bean;
  }

  /**
   * Some bean to add data mappers.
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
}
