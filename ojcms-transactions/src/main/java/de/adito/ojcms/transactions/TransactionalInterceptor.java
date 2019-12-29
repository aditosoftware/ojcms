package de.adito.ojcms.transactions;

import de.adito.ojcms.cdi.*;
import de.adito.ojcms.transactions.annotations.*;
import de.adito.ojcms.transactions.api.*;

import javax.inject.Inject;
import javax.interceptor.*;
import java.util.Optional;
import java.util.logging.*;

import static de.adito.ojcms.transactions.annotations.ETransactionMode.REQUIRES_NEW;

/**
 * Interceptor for methods or classes annotated by {@link Transactional}.
 * Starts the transactional context and commits or rolls back the changes after the execution of the transaction.
 *
 * @author Simon Danner, 25.12.2019
 */
@Transactional
@Interceptor
class TransactionalInterceptor
{
  private static final Logger LOGGER = Logger.getLogger(TransactionalInterceptor.class.getName());

  @Inject
  private ICdiControl cdiControl;
  @Inject
  private TransactionManager transactionManager;

  @AroundInvoke
  private Object _manageTransaction(InvocationContext pInvocation)
  {
    final Transactional annotation = _retrieveAnnotation(pInvocation);
    final int tries = annotation.tries();

    if (tries < 1)
      throw new IllegalArgumentException("Bad try count for transaction: " + tries);

    int tryCount = 0;

    while (tryCount < tries)
    {
      if (tryCount > 0)
        LOGGER.info("Retrying transaction...");

      final Optional<IActiveContext> activeContext = _tryToStartNewContext(annotation.mode() == REQUIRES_NEW);

      try
      {
        final Object result = pInvocation.proceed();
        transactionManager.commitChanges();
        return result;
      }
      catch (ConcurrentTransactionException pConcurrentException)
      {
        LOGGER.log(Level.WARNING, "Transaction failed due to concurrent bean modification! Reason: " + pConcurrentException.getMessage());
        tryCount++;
      }
      catch (Exception pE)
      {
        transactionManager.rollbackChanges();
        throw new TransactionFailedException(pE);
      }
      finally
      {
        activeContext.ifPresent(IActiveContext::destroy);
      }
    }

    transactionManager.rollbackChanges();
    throw new TransactionFailedException(tries);
  }

  /**
   * Tries to start the transactional context. If there is already an active transaction and a new one isn't requested, nothing happens.
   *
   * @param pRequiresNew <tt>true</tt> if a new transaction is required by request
   * @return an optional control element of the newly started transaction
   */
  private Optional<IActiveContext> _tryToStartNewContext(boolean pRequiresNew)
  {
    if (!pRequiresNew && cdiControl.isContextActive(TransactionalScoped.class))
      return Optional.empty();

    return Optional.of(cdiControl.startContext(TransactionalScoped.class));
  }

  /**
   * Retrieves the {@link Transactional} annotation either from the method or class.
   *
   * @param pInvocation the method invocation that triggered this interceptor
   * @return the transactional annotation
   */
  private Transactional _retrieveAnnotation(InvocationContext pInvocation)
  {
    return Optional.ofNullable(pInvocation.getMethod().getAnnotation(Transactional.class)) //
        .orElseGet(() -> pInvocation.getTarget().getClass().getAnnotation(Transactional.class));
  }
}
