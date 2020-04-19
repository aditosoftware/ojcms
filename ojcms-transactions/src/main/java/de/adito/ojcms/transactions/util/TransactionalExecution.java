package de.adito.ojcms.transactions.util;

import de.adito.ojcms.cdi.ICdiControl;
import de.adito.ojcms.transactions.annotations.Transactional;

import javax.enterprise.context.ApplicationScoped;
import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * An injectable helper to run code within a new transactional context.
 * This is then useful if you need transactional handling in non CDI managed instances.
 * You can use this helper via {@link ICdiControl#createInjected(Class, Annotation...)}.
 *
 * @author Simon Danner, 15.04.2020
 */
@ApplicationScoped
public class TransactionalExecution
{
  /**
   * Runs some no result action within a new transaction.
   *
   * @param pAction the action to execute within the new transaction
   */
  @Transactional
  public void justRun(Runnable pAction)
  {
    pAction.run();
  }

  /**
   * Runs some no result action that may throw one exception type within a new transaction.
   *
   * @param pAction     the action to execute within the new transaction
   * @param <EXCEPTION> the type of the checked exception that may occur
   */
  @Transactional
  public <EXCEPTION extends Exception> void justRunThrowing(ThrowingAction<EXCEPTION> pAction) throws EXCEPTION
  {
    pAction.run();
  }

  /**
   * Runs some no result action that may throw two exception types within a new transaction.
   *
   * @param pAction       the action to execute within the new transaction
   * @param <EXCEPTION_1> the type of the first checked exception that may occur
   * @param <EXCEPTION_2> the type of the second checked exception that may occur
   */
  @Transactional
  public <EXCEPTION_1 extends Exception, EXCEPTION_2 extends Exception> void justRunTwoThrowing(
      TwoThrowingAction<EXCEPTION_1, EXCEPTION_2> pAction) throws EXCEPTION_1, EXCEPTION_2
  {
    pAction.run();
  }

  /**
   * Performs an action that supplies a specific result within a new transaction.
   *
   * @param pResultAction the result provider
   * @return the retrieved result
   */
  @Transactional
  public <RESULT> RESULT resolveResult(Supplier<RESULT> pResultAction)
  {
    return pResultAction.get();
  }

  /**
   * Performs an action that supplies a specific result within a new transaction.
   * The action may throw a specific exception.
   *
   * @param pResultAction the result provider
   * @param <EXCEPTION>   the exception type that may occur
   * @return the retrieved result
   */
  @Transactional
  public <RESULT, EXCEPTION extends Exception> RESULT resolveResultThrowing(ThrowingResultSupplier<RESULT, EXCEPTION> pResultAction)
      throws EXCEPTION
  {
    return pResultAction.resolveResult();
  }

  /**
   * Performs an action that supplies a specific result within a new transaction.
   * The action may throw two specific exception types.
   *
   * @param pResultAction the result provider
   * @param <EXCEPTION_1> the first exception type that may occur
   * @param <EXCEPTION_2> the second exception type that may occur
   * @return the retrieved result
   */
  @Transactional
  public <RESULT, EXCEPTION_1 extends Exception, EXCEPTION_2 extends Exception> RESULT resolveResultTwoThrowing(
      TwoThrowingResultSupplier<RESULT, EXCEPTION_1, EXCEPTION_2> pResultAction) throws EXCEPTION_1, EXCEPTION_2
  {
    return pResultAction.resolveResult();
  }

  /**
   * Defines a no result action that may throw a specific exception.
   */
  @FunctionalInterface
  public interface ThrowingAction<EXCEPTION extends Exception>
  {
    /**
     * Performs the action.
     */
    void run() throws EXCEPTION;
  }

  /**
   * Defines a no result action that may throw two specific exception types.
   */
  @FunctionalInterface
  public interface TwoThrowingAction<EXCEPTION_1 extends Exception, EXCEPTION_2 extends Exception>
  {
    /**
     * Performs the action.
     */
    void run() throws EXCEPTION_1, EXCEPTION_2;
  }

  /**
   * Defines an action that provides a specific result and may throw a specific exception.
   */
  @FunctionalInterface
  public interface ThrowingResultSupplier<RESULT, EXCEPTION extends Exception>
  {
    /**
     * Resolves the result.
     */
    RESULT resolveResult() throws EXCEPTION;
  }

  /**
   * Defines an action that provides a specific result and may throw two specific exception types.
   */
  @FunctionalInterface
  public interface TwoThrowingResultSupplier<RESULT, EXCEPTION_1 extends Exception, EXCEPTION_2 extends Exception>
  {
    /**
     * Resolves the result.
     */
    RESULT resolveResult() throws EXCEPTION_1, EXCEPTION_2;
  }
}
