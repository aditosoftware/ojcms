package de.adito.ojcms.transactions.exceptions;

/**
 * Indicates that an transaction has failed.
 *
 * @author Simon Danner, 27.12.2019
 */
public class TransactionFailedException extends RuntimeException
{
  /**
   * Initializes the exception with an error message indicating that it failed due to an unexpected {@link Throwable}.
   *
   * @param pCause the unexpected cause of the failure
   */
  public TransactionFailedException(Throwable pCause)
  {
    super("Transaction failed unexpectedly! Not retries will be scheduled!", pCause);
  }

  /**
   * Initializes the exception with an error message indicating that it failed after an amount of tries.
   *
   * @param pTryCount the amount of tries
   */
  public TransactionFailedException(int pTryCount)
  {
    super("Transaction not successful after " + pTryCount + " tries!");
  }
}
