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
  private static final String SINGLE_BEAN_ID = "singleBeanId";
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
  }

  @Test
  public void testContainerSizeRequest()
  {
    assertEquals(CONTAINER_SIZE, transaction.requestContainerSize(CONTAINER_ID));
  }

  @Test
  public void testContainerBeanDataRequestByIndex()
  {
    final ContainerIndexKey key = new ContainerIndexKey(CONTAINER_ID, 0);
    final BeanData<ContainerIndexKey> beanData = transaction.requestBeanDataFromContainer(key);

    assertEquals(key, beanData.getKey());
    _checkBeanData(beanData);
  }

  @Test
  public void testContainerBeanDataRequestByIdentifiers()
  {
    final ContainerIdentifierKey key = new ContainerIdentifierKey(CONTAINER_ID, Collections.emptyMap());
    final BeanData<ContainerIndexKey> beanData = transaction.requestBeanDataFromContainer(key);

    assertEquals(key, beanData.getIdentifierKey(CONTAINER_ID));
    _checkBeanData(beanData);
  }

  @Test
  public void testSingleBeanDataRequest()
  {
    final BeanData<String> beanData = transaction.requestSingleBeanData(SINGLE_BEAN_ID);
    assertEquals(SINGLE_BEAN_ID, beanData.getKey());
    _checkBeanData(beanData);
  }

  @Test
  public void testCommitChanges()
  {
    _registerChanges();
    transactionManager.commitChanges();

    verify(beanDataStorageMock).processAdditionsForContainer(any(), any());
    verify(beanDataStorageMock).processRemovalsById(any());
    verify(beanDataStorageMock).processRemovalsByIdentifiers(any());
    verify(beanDataStorageMock).processChangesForBean(any(), any());
    verify(beanDataStorageMock).processChangesForSingleBean(any(), any());
  }

  @Test
  public void testRollback()
  {
    _registerChanges();
    transactionManager.rollbackChanges();
    verifyZeroInteractions(beanDataStorageMock);
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
    transaction.registerSingleBeanValueChange(SINGLE_BEAN_ID, BEAN_FIELD, 12);
    final BeanData<String> beanData = transaction.requestSingleBeanData(SINGLE_BEAN_ID);
    _checkBeanData(beanData);
  }

  private void _checkBeanData(BeanData<?> pResult)
  {
    assertEquals(1, pResult.getData().size());
    final Map.Entry<IField<?>, Object> firstEntry = pResult.getData().entrySet().iterator().next();
    assertSame(BEAN_FIELD, firstEntry.getKey());
    assertEquals(BEAN_VALUE, firstEntry.getValue());
  }

  private void _registerChanges()
  {
    transaction.registerBeanAddition(CONTAINER_ID, 1, Collections.emptyMap());
    transaction.registerBeanRemoval(new ContainerIndexKey(CONTAINER_ID, 1));
    transaction.registerBeanRemoval(new ContainerIdentifierKey(CONTAINER_ID, Collections.emptyMap()));
    transaction.registerBeanValueChange(new ContainerIndexKey(CONTAINER_ID, 0), BEAN_FIELD, 6);
    transaction.registerSingleBeanValueChange(SINGLE_BEAN_ID, BEAN_FIELD, 7);
  }

  @Alternative
  @Priority(100)
  static class MockedBeanLoader implements IBeanDataLoader
  {
    private static final Map<IField<?>, Object> BEAN_DATA = Collections.singletonMap(BEAN_FIELD, BEAN_VALUE);

    @Override
    public int loadSize(String pContainerId)
    {
      return CONTAINER_SIZE;
    }

    @Override
    public BeanData<ContainerIndexKey> loadByIndex(ContainerIndexKey pKey)
    {
      return new BeanData<>(pKey, BEAN_DATA);
    }

    @Override
    public BeanData<ContainerIndexKey> loadByIdentifiers(ContainerIdentifierKey pKey)
    {
      return new BeanData<>(new ContainerIndexKey(pKey.getContainerId(), 0), BEAN_DATA);
    }

    @Override
    public BeanData<String> loadSingleBean(String pKey)
    {
      return new BeanData<>(pKey, BEAN_DATA);
    }
  }
}
