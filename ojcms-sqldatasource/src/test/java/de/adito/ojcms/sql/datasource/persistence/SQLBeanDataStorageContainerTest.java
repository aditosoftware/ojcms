package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.sql.datasource.model.*;
import de.adito.ojcms.transactions.api.*;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the {@link SQLBeanDataStorage} for bean containers with an actual database connection (in-memory).
 *
 * @author Simon Danner, 06.01.2020
 */
public class SQLBeanDataStorageContainerTest extends AbstractDatabaseTest<ContainerPersistenceModel>
{
  @Inject
  private SQLBeanDataLoader loader;
  @Inject
  private SQLBeanDataStorage storage;

  @Test
  public void testProcessChangesForBean()
  {
    addContentToContainer(0, 42, "42", true);
    final BeanIndexKey indexKey = new BeanIndexKey(CONTAINER_ID, 0);

    storage.processChangesForBean(indexKey, Collections.singletonMap(SomeBean.FIELD1, 22));
    final PersistentBeanData changedData1 = loader.loadByKey(indexKey);

    assertNotNull(changedData1);
    assertEquals(0, changedData1.getIndex());
    assertEquals(22, changedData1.getData().get(SomeBean.FIELD1));

    final BeanIdentifiersKey identifiersKey = createIdentifiersKey(22, "42");
    final Map<IField<?>, Object> changes = new HashMap<>();
    changes.put(SomeBean.FIELD2, "22");
    changes.put(SomeBean.FIELD3, false);

    storage.processChangesForBean(identifiersKey, changes);
    final PersistentBeanData changedData2 = loader.loadByKey(indexKey);
    assertNotNull(changedData2);
    assertEquals("22", changedData2.getData().get(SomeBean.FIELD2));
    assertEquals(false, changedData2.getData().get(SomeBean.FIELD3));
  }

  @Test
  public void testProcessAdditionsForContainer()
  {
    final PersistentBeanData expectedData1 = addContentToContainer(0, 1, "1", false);
    final PersistentBeanData expectedData2 = addContentToContainer(1, 2, "2", true);

    assertEquals(2, loader.loadContainerSize(CONTAINER_ID));

    final PersistentBeanData data1 = loader.loadByKey(new BeanIndexKey(CONTAINER_ID, 0));
    final PersistentBeanData data2 = loader.loadByKey(new BeanIndexKey(CONTAINER_ID, 1));
    assertEquals(expectedData1, data1);
    assertEquals(expectedData2, data2);
  }

  @Test
  public void testProcessRemovals()
  {
    addContentToContainer(0, 1, "1", false);
    addContentToContainer(1, 2, "2", true);
    final PersistentBeanData expectedData = addContentToContainer(2, 3, "3", true);

    assertEquals(3, loader.loadContainerSize(CONTAINER_ID));

    final Set<IContainerBeanKey> keys = new HashSet<>();
    keys.add(new BeanIndexKey(CONTAINER_ID, 1));
    keys.add(createIdentifiersKey(1, "1"));

    storage.processRemovals(keys);

    assertEquals(1, loader.loadContainerSize(CONTAINER_ID));
    final PersistentBeanData data = loader.loadByKey(new BeanIndexKey(CONTAINER_ID, 0));
    assertEquals(0, data.getIndex());
    assertEquals(expectedData.getData(), data.getData());
  }

  @Override
  protected Class<ContainerPersistenceModel> getModelType()
  {
    return ContainerPersistenceModel.class;
  }
}
