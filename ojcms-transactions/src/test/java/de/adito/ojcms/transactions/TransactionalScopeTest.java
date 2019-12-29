package de.adito.ojcms.transactions;

import de.adito.ojcms.cdi.AbstractCdiTest;
import de.adito.ojcms.transactions.annotations.*;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.*;
import javax.inject.Inject;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the transactional scope.
 *
 * @author Simon Danner, 25.12.2019
 * @see TransactionalScoped
 */
public class TransactionalScopeTest extends AbstractCdiTest
{
  @Inject
  private SomeBean bean;
  @Inject
  private SomeBusinessCode businessCode;
  @Inject
  private SomeOtherBusinessCode otherBusinessCode;

  @Test
  public void testTransactionChange()
  {
    final int localId = bean.getId();
    final int transactionId = businessCode.getIdWithinNewTransaction();
    assertNotEquals(localId, transactionId);
  }

  @Test
  public void testSameTransactionRequires()
  {
    final int localId = bean.getId();
    final int transactionId = businessCode.getIdWithinTransaction();
    assertEquals(localId, transactionId);
  }

  @Test
  public void testSameTransactionNoChange()
  {
    final int localId = bean.getId();
    final int noNewTransactionId = businessCode.getIdWithoutNewTransaction();
    assertEquals(localId, noNewTransactionId);
  }

  @Test
  public void testNewThreadNoContext()
  {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    executorService.submit(() -> assertThrows(ContextNotActiveException.class, () -> businessCode.getIdWithoutNewTransaction()));
  }

  @Test
  public void testNewThreadNewTransaction() throws ExecutionException, InterruptedException
  {
    final int localId = bean.getId();
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Future<Integer> async = executorService.submit(() -> businessCode.getIdWithinNewTransaction());
    assertNotEquals(localId, async.get());
  }

  @Test
  public void testTransactionChangeAnnotationOnClassLevel()
  {
    final int localId = bean.getId();
    final int transactionId = otherBusinessCode.getIdWithinNewTransactionViaClassAnnotation();
    assertNotEquals(localId, transactionId);
  }

  @TransactionalScoped
  static class SomeBean
  {
    private static int nextId = 0;
    private final int id;

    SomeBean()
    {
      id = nextId++;
    }

    int getId()
    {
      return id;
    }
  }

  @ApplicationScoped
  static class SomeBusinessCode
  {
    @Inject
    private SomeBean bean;

    @Transactional
    int getIdWithinNewTransaction()
    {
      return bean.getId();
    }

    @Transactional(mode = ETransactionMode.REQUIRES)
    int getIdWithinTransaction()
    {
      return bean.getId();
    }

    int getIdWithoutNewTransaction()
    {
      return bean.getId();
    }
  }

  @ApplicationScoped
  @Transactional
  static class SomeOtherBusinessCode
  {
    @Inject
    private SomeBean bean;

    int getIdWithinNewTransactionViaClassAnnotation()
    {
      return bean.getId();
    }
  }
}
