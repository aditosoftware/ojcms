package de.adito.ojcms.sql.datasource.model;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.transactions.api.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Special tests for persistent base container models.
 *
 * @author Simon Danner, 18.03.2020
 */
public class BaseContainerPersistenceModelTest extends AbstractDatabaseTest<BaseContainerPersistenceModel>
{
  @Test
  public void testInitModelInDatabase()
  {
    final Set<Class<? extends IBean>> subTypes = new HashSet<>(Arrays.asList(SomeOtherBean.class, SomeSpecialBean.class));
    final BaseContainerPersistenceModel otherModel = new BaseContainerPersistenceModel(CONTAINER_ID + 2, subTypes);
    otherModel.initModelInDatabase(builder);
    otherModel.initModelInDatabase(builder); //Try it another time

    assertTrue(builder.hasTable(CONTAINER_ID + 2));
    assertEquals(8, builder.getColumnCount(CONTAINER_ID + 2)); //Five bean fields + id, index and type column -> 8

    subTypes.remove(SomeSpecialBean.class);
    final BaseContainerPersistenceModel otherModel2 = new BaseContainerPersistenceModel(CONTAINER_ID + 2, subTypes);
    otherModel2.initModelInDatabase(builder);
    assertEquals(7, builder.getColumnCount(CONTAINER_ID + 2)); //Four bean fields + id, index and type column -> 7
  }

  @Test
  public void testLoadBeanType()
  {
    addContentToBaseContainer(0, new SomeOtherBean(1, "1", true, 2));
    addContentToBaseContainer(1, new SomeSpecialBean(2, "2", false, 3, "3"));

    final Class<IBean> type1 = model.loadBeanType(new InitialIndexKey(CONTAINER_ID, 0), builder);
    final Class<IBean> type2 = model.loadBeanType(new InitialIndexKey(CONTAINER_ID, 1), builder);
    assertSame(SomeOtherBean.class, type1);
    assertSame(SomeSpecialBean.class, type2);
  }

  @Test
  public void testLoadData()
  {
    final List<PersistentBeanData> added = _addSomeContent();

    final PersistentBeanData result = model.loadDataByIndex(new InitialIndexKey(CONTAINER_ID, 0), builder);
    assertEquals(added.get(0), result);
    final PersistentBeanData result2 = model.loadDataByIdentifiers(createIdentifiers(2, "2"), builder) //
        .orElseThrow(AssertionError::new);
    assertEquals(added.get(1), result2);
    final PersistentBeanData result3 = model.loadDataByIndex(new InitialIndexKey(CONTAINER_ID, 2), builder);
    assertEquals(added.get(2), result3);
  }

  @Override
  protected Class<BaseContainerPersistenceModel> getModelType()
  {
    return BaseContainerPersistenceModel.class;
  }

  /**
   * Adds some content/beans to the container.
   *
   * @return the list of added bean data
   */
  private List<PersistentBeanData> _addSomeContent()
  {
    final List<PersistentBeanData> content = new ArrayList<>();
    content.add(addContentToBaseContainer(0, new SomeSpecialBean(1, "1", true, 1, "1")));
    content.add(addContentToBaseContainer(1, new SomeSpecialBean(2, "2", false, 2, "2")));
    content.add(addContentToBaseContainer(2, new SomeOtherBean(3, "3", true, 3)));

    return content;
  }
}
