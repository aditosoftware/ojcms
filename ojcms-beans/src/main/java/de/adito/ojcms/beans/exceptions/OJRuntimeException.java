package de.adito.ojcms.beans.exceptions;

/**
 * Default runtime exception for the OJ framework.
 * This base type should be used if the cause of the exception is a result of an incorrect usage of the framework.
 * In other words: This exception is a hint for a bug that should not appear in productive applications,
 * but isn't an internal bug of the OJ framework.
 *
 * @author Simon Danner, 23.12.2018
 */
public class OJRuntimeException extends RuntimeException
{
  /**
   * Creates new exception with a detail message.
   *
   * @param pDetailMessage a detailed message describing the cause of the exception
   */
  public OJRuntimeException(String pDetailMessage)
  {
    super(pDetailMessage);
  }

  /**
   * Creates new exception with a throwable cause.
   *
   * @param pCause the cause of the exception
   */
  public OJRuntimeException(Throwable pCause)
  {
    super(pCause);
  }
}
