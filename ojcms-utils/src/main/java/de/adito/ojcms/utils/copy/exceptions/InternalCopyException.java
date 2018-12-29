package de.adito.ojcms.utils.copy.exceptions;

/**
 * Exception for internal runtime exception while copying.
 *
 * @author Simon Danner, 28.12.2018
 */
public class InternalCopyException extends RuntimeException
{
  /**
   * Creates a new internal exception.
   *
   * @param pCause the cause of the exception
   */
  public InternalCopyException(Throwable pCause)
  {
    super(pCause);
  }
}
