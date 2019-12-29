package de.adito.ojcms.transactions;

import de.adito.ojcms.cdi.AbstractCdiTest;
import de.adito.ojcms.transactions.annotations.*;
import de.adito.ojcms.transactions.api.*;
import org.junit.jupiter.api.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the transactional interceptor.
 *
 * @author Simon Danner, 29.12.2019
 * @see Transactional
 */
public class TransactionalInterceptorTest extends AbstractCdiTest
{
  private static final int MAGIC_RESULT = 42;

  @Inject
  private SomeBusinessCode businessCode;

  @BeforeEach
  public void resetTryCounter()
  {
    TryCounter.count = 0;
  }

  @Test
  public void testBadTryCount()
  {
    assertThrows(IllegalArgumentException.class, () -> businessCode.badTries());
    assertThrows(IllegalArgumentException.class, () -> businessCode.evenMoreBadTries());
  }

  @Test
  public void testTransactionRetrySuccess()
  {
    assertEquals(MAGIC_RESULT, businessCode.doSomething(2));
    assertEquals(2, TryCounter.count);
  }

  @Test
  public void testTransactionRetryFail()
  {
    assertThrows(TransactionFailedException.class, () -> businessCode.doSomething(4));
    assertEquals(3, TryCounter.count);
  }

  @Test
  public void testNoRetryAfterUnexpectedFailure()
  {
    assertThrows(TransactionFailedException.class, () -> businessCode.doSomethingDifficult());
    assertEquals(1, TryCounter.count);
  }

  @ApplicationScoped
  static class SomeBusinessCode
  {
    @Inject
    private TryCounter tryCounter;

    @Transactional(tries = 3)
    int doSomething(int successAfterTries)
    {
      tryCounter.forceInitialization();

      if (TryCounter.count < successAfterTries)
        throw new ConcurrentTransactionException("key");

      return MAGIC_RESULT;
    }

    @Transactional(tries = 3)
    int doSomethingDifficult()
    {
      tryCounter.forceInitialization();
      throw new RuntimeException();
    }

    @Transactional(tries = 0)
    void badTries()
    {
    }

    @Transactional(tries = -2)
    void evenMoreBadTries()
    {
    }
  }

  @TransactionalScoped
  static class TryCounter
  {
    static int count = 0;

    TryCounter()
    {
      count++;
    }

    void forceInitialization()
    {
    }
  }
}
