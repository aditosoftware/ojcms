package de.adito.ojcms.utils.copy.exceptions;

/**
 * Exception for unsupported copy operations.
 *
 * @author Simon Danner, 28.12.2018
 */
public class CopyUnsupportedException extends Exception
{
  /**
   * Creates the copy unsupported exception with a detail message and a cause for the failure.
   *
   * @param pDetailMessage a detailed message describing the cause of the exception
   * @param pCause         the cause of the exception
   */
  public CopyUnsupportedException(String pDetailMessage, Throwable pCause)
  {
    super(pDetailMessage, pCause);
  }
}
