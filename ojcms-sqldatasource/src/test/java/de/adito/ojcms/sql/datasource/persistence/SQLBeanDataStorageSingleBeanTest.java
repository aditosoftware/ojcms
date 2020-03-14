package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.sql.datasource.model.*;
import de.adito.ojcms.transactions.api.SingleBeanKey;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link SQLBeanDataLoader} for single beans with an actual database connection (in-memory).
 *
 * @author Simon Danner, 06.01.2020
 */
public class SQLBeanDataStorageSingleBeanTest extends AbstractDatabaseTest<SingleBeanPersistenceModel>
{
  @Inject
  private SQLBeanDataLoader loader;

  @Test
  public void testProcessChangesForBean()
  {
    final Map<IField<?>, Object> initialData = _loadSingleBeanData();
    assertEquals(3, initialData.size());

    setSingleBeanValues(42, "42", true);
    final Map<IField<?>, Object> data = _loadSingleBeanData();

    assertEquals(3, data.size());
    assertEquals(42, data.get(SomeBean.FIELD1));
    assertEquals("42", data.get(SomeBean.FIELD2));
    assertEquals(true, data.get(SomeBean.FIELD3));

    setSingleBeanValues(100, "100", false);
    final Map<IField<?>, Object> data2 = _loadSingleBeanData();

    assertEquals(3, data2.size());
    assertEquals(100, data2.get(SomeBean.FIELD1));
    assertEquals("100", data2.get(SomeBean.FIELD2));
    assertEquals(false, data2.get(SomeBean.FIELD3));
  }

  /**
   * Loads the data of the single bean of this test.
   *
   * @return the bean data as map
   */
  private Map<IField<?>, Object> _loadSingleBeanData()
  {
    return loader.loadSingleBeanData(new SingleBeanKey(CONTAINER_ID)).getData();
  }

  @Override
  protected Class<SingleBeanPersistenceModel> getModelType()
  {
    return SingleBeanPersistenceModel.class;
  }
}
