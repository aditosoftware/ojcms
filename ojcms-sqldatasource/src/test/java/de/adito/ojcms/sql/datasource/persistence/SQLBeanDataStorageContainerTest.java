package de.adito.ojcms.sql.datasource.persistence;

import de.adito.ojcms.sql.datasource.model.*;
import de.adito.ojcms.transactions.api.*;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Collections;

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
    final InitialIndexKey indexKey = new InitialIndexKey(CONTAINER_ID, 0);

    storage.processChangesForContainerBean(indexKey, Collections.singletonMap(SomeBean.FIELD1, 22));
    final PersistentBeanData changedData1 = loader.loadContainerBeanDataByIndex(indexKey);

    assertNotNull(changedData1);
    assertEquals(0, changedData1.getIndex());
    assertEquals(22, changedData1.getData().get(SomeBean.FIELD1));
  }

  @Test
  public void testProcessAdditionsForContainer()
  {
    final PersistentBeanData expectedData1 = addContentToContainer(0, 1, "1", false);
    final PersistentBeanData expectedData2 = addContentToContainer(1, 2, "2", true);

    assertEquals(2, loader.loadContainerSize(CONTAINER_ID));

    final PersistentBeanData data1 = loader.loadContainerBeanDataByIndex(new InitialIndexKey(CONTAINER_ID, 0));
    final PersistentBeanData data2 = loader.loadContainerBeanDataByIndex(new InitialIndexKey(CONTAINER_ID, 1));
    assertEquals(expectedData1, data1);
    assertEquals(expectedData2, data2);

    addContentToContainer(0, 3, "3", false);
    assertEquals(3, loader.loadContainerSize(CONTAINER_ID));
    final PersistentBeanData newData2 = loader.loadContainerBeanDataByIndex(new InitialIndexKey(CONTAINER_ID, 1));
    assertEquals(1, newData2.getIndex());
    assertEquals(expectedData1.getData(), newData2.getData());
  }

  @Test
  public void testProcessRemovals()
  {
    addContentToContainer(0, 1, "1", false);
    addContentToContainer(1, 2, "2", true);
    final PersistentBeanData expectedData = addContentToContainer(2, 3, "3", true);
    assertEquals(3, loader.loadContainerSize(CONTAINER_ID));

    storage.processRemovals(Collections.singletonMap(CONTAINER_ID, Collections.singleton(new InitialIndexKey(CONTAINER_ID, 1))));

    assertEquals(2, loader.loadContainerSize(CONTAINER_ID));
    final PersistentBeanData data = loader.loadContainerBeanDataByIndex(new InitialIndexKey(CONTAINER_ID, 1));
    assertEquals(1, data.getIndex());
    assertEquals(expectedData.getData(), data.getData());
  }

  @Override
  protected Class<ContainerPersistenceModel> getModelType()
  {
    return ContainerPersistenceModel.class;
  }
}
