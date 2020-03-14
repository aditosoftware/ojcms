package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;
import de.adito.ojcms.sqlbuilder.definition.condition.IWhereCondition;
import de.adito.ojcms.sqlbuilder.result.ResultRow;
import de.adito.ojcms.transactions.api.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static de.adito.ojcms.sql.datasource.util.DatabaseConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link SingleBeanPersistenceModel} with an actual database connection (in-memory).
 *
 * @author Simon Danner, 05.01.2020
 */
public class SingleBeanPersistenceModelTest extends AbstractDatabaseTest<SingleBeanPersistenceModel>
{
  private static final String BEAN_ID = "beanId";

  @Test
  public void testInitModelInDatabase()
  {
    final SingleBeanPersistenceModel otherModel = new SingleBeanPersistenceModel(BEAN_ID + 2, SomeBean.class);
    otherModel.initModelInDatabase(builder);

    assertTrue(builder.hasTable(BEAN_TABLE_NAME));

    final IColumnIdentification<String> idColumn = IColumnIdentification.of(BEAN_TABLE_BEAN_ID, String.class);

    final Optional<ResultRow> result = builder.doSelect(pSelect -> pSelect
        .select(idColumn)
        .from(BEAN_TABLE_NAME)
        .where(IWhereCondition.isEqual(idColumn, BEAN_ID + 2))
        .firstResult());

    assertTrue(result.isPresent());
  }

  @Test
  public void testLoadDataByKey()
  {
    //First check initial state
    final PersistentBeanData initialData = model.loadSingleBeanData(new SingleBeanKey(CONTAINER_ID), builder);
    assertEquals(-1, initialData.getIndex());
    final Map<IField<?>, Object> initialValues = initialData.getData();
    assertEquals(3, initialValues.size());
    initialValues.forEach(((pField, pValue) -> assertEquals(pField.getInitialValue(), pValue)));

    setSingleBeanValues(42, "42", true);
    final PersistentBeanData changedData = model.loadSingleBeanData(new SingleBeanKey(CONTAINER_ID), builder);
    assertEquals(-1, changedData.getIndex());
    final Map<IField<?>, Object> changedValues = changedData.getData();
    assertEquals(3, changedValues.size());
    assertEquals(42, changedValues.get(SomeBean.FIELD1));
    assertEquals("42", changedValues.get(SomeBean.FIELD2));
    assertEquals(true, changedValues.get(SomeBean.FIELD3));
  }

  @Override
  protected Class<SingleBeanPersistenceModel> getModelType()
  {
    return SingleBeanPersistenceModel.class;
  }
}
