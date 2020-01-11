package de.adito.ojcms.transactions.exceptions;

/**
 * Indicates that the data of a bean has been modified by another transaction.
 *
 * @author Simon Danner, 27.12.2019
 */
public class ConcurrentTransactionException extends RuntimeException
{
  /**
   * Initializes the exception by providing the generic key of the bean that has changed concurrently.
   *
   * @param pKey a key identifying the changed bean
   */
  public ConcurrentTransactionException(Object pKey)
  {
    super("Bean or BeanContainer with key " + pKey + " has been modified by an other active transaction!");
  }
}
