package de.adito.ojcms.transactions;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.cdi.*;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.*;
import org.jboss.weld.proxy.WeldClientProxy;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Black box test for {@link ITransaction}.
 *
 * @author Simon Danner, 29.12.2019
 */
public class BeanTransactionTest extends AbstractCdiTest
{
  private static final String CONTAINER_ID = "containerId";
  private static final SingleBeanKey SINGLE_BEAN_KEY = new SingleBeanKey("singleBeanId");
  private static final int CONTAINER_SIZE = 7;
  @SuppressWarnings("unchecked")
  private static final IField<Integer> BEAN_FIELD = Mockito.mock(IField.class);
  private static final int BEAN_VALUE = 42;

  @Inject
  private ICdiControl cdiControl;
  @Inject
  private TransactionManager transactionManager;
  @Inject
  private ITransaction transaction;
  @Inject
  private IBeanDataStorage beanDataStorage;

  private IBeanDataStorage beanDataStorageMock;

  @BeforeEach
  public void setup()
  {
    beanDataStorageMock = (IBeanDataStorage) ((WeldClientProxy) beanDataStorage).getMetadata().getContextualInstance();
    reset(beanDataStorageMock);
  }

  @Test
  public void testContainerSizeRequest()
  {
    assertEquals(CONTAINER_SIZE, transaction.requestContainerSize(CONTAINER_ID));
  }

  @Test
  public void testContainerBeanDataRequestByIndex()
  {
    final BeanIndexKey key = new BeanIndexKey(CONTAINER_ID, 0);
    final PersistentBeanData beanData = transaction.requestBeanDataByKey(key);

    assertEquals(0, beanData.getIndex());
    _checkBeanData(beanData);
  }

  @Test
  public void testContainerBeanDataRequestByIdentifiers()
  {
    final BeanIdentifiersKey key = new BeanIdentifiersKey(CONTAINER_ID, Collections.emptyMap());
    final PersistentBeanData beanData = transaction.requestBeanDataByKey(key);

    assertEquals(key, beanData.createIdentifierKey(CONTAINER_ID));
    _checkBeanData(beanData);
  }

  @Test
  public void testSingleBeanDataRequest()
  {
    final PersistentBeanData beanData = transaction.requestBeanDataByKey(SINGLE_BEAN_KEY);
    _checkBeanData(beanData);
  }

  @Test
  public void testRequestContainerSizeAfterChangeInSameTransaction()
  {
    transaction.registerBeanAddition(CONTAINER_ID, 1, Collections.emptyMap());
    transaction.registerBeanAddition(CONTAINER_ID, 2, Collections.emptyMap());
    transaction.registerBeanRemoval(new BeanIndexKey(CONTAINER_ID, 0));

    final int size = transaction.requestContainerSize(CONTAINER_ID);
    assertEquals(CONTAINER_SIZE + 1, size);
  }

  @Test
  public void testRequestValueAfterChangeInSameTransaction()
  {
    final BeanIndexKey key = new BeanIndexKey(CONTAINER_ID, 0);
    transaction.registerBeanValueChange(key, BEAN_FIELD, 100);
    final Object value = transaction.requestBeanDataByKey(key).getData().get(BEAN_FIELD);
    assertEquals(100, value);
  }

  @Test
  public void testCommitChanges()
  {
    _registerChanges();
    transactionManager.commitChanges();

    verify(beanDataStorageMock).processAdditionsForContainer(any(), any());
    verify(beanDataStorageMock).processRemovals(any());
    verify(beanDataStorageMock, times(2)).processChangesForBean(any(), any());
  }

  @Test
  public void testRollback()
  {
    _registerChanges();
    transactionManager.rollbackChanges();
    verify(beanDataStorageMock).rollbackChanges();
  }

  @Test
  public void testAvoidConcurrentModificationMultipleTransactions()
  {
    transaction.registerBeanAddition(CONTAINER_ID, 1, Collections.emptyMap());
    cdiControl.startContext(TransactionalScoped.class);
    transaction.requestContainerSize(CONTAINER_ID);
  }

  @Test
  public void testConcurrentModificationOkayInSameTransaction()
  {
    transaction.registerBeanValueChange(SINGLE_BEAN_KEY, BEAN_FIELD, 12);
    final PersistentBeanData beanData = transaction.requestBeanDataByKey(SINGLE_BEAN_KEY);
    assertNotNull(beanData);
  }

  private void _checkBeanData(PersistentBeanData pResult)
  {
    assertEquals(1, pResult.getData().size());
    final Map.Entry<IField<?>, Object> firstEntry = pResult.getData().entrySet().iterator().next();
    assertSame(BEAN_FIELD, firstEntry.getKey());
    assertEquals(BEAN_VALUE, firstEntry.getValue());
  }

  private void _registerChanges()
  {
    transaction.registerBeanAddition(CONTAINER_ID, 1, Collections.emptyMap());
    transaction.registerBeanRemoval(new BeanIndexKey(CONTAINER_ID, 1));
    transaction.registerBeanRemoval(new BeanIdentifiersKey(CONTAINER_ID, Collections.emptyMap()));
    transaction.registerBeanValueChange(new BeanIndexKey(CONTAINER_ID, 0), BEAN_FIELD, 6);
    transaction.registerBeanValueChange(SINGLE_BEAN_KEY, BEAN_FIELD, 7);
  }

  @Alternative
  @Priority(100)
  static class MockedBeanLoader implements IBeanDataLoader
  {
    private static final Map<IField<?>, Object> BEAN_DATA = Collections.singletonMap(BEAN_FIELD, BEAN_VALUE);

    @Override
    public int loadContainerSize(String pContainerId)
    {
      return CONTAINER_SIZE;
    }

    @Override
    public <KEY extends IBeanKey> PersistentBeanData loadByKey(KEY pKey)
    {
      return new PersistentBeanData(0, BEAN_DATA);
    }

    @Override
    public Map<Integer, PersistentBeanData> fullContainerLoad(String pContainerId)
    {
      return Collections.singletonMap(0, loadByKey(null));
    }
  }
}
