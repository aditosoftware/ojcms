package de.adito.ojcms.beans.exceptions;

/**
 * Runtime exception for internal bugs of the OJ framework.
 * Such exceptions shouldn't even appear for the users of the framework.
 *
 * @author Simon Danner, 23.12.2018
 */
public class OJInternalException extends RuntimeException
{
  /**
   * Create a new internal exception with a detail message.
   *
   * @param pDetailMessage a detailed message describing the cause of the exception
   */
  public OJInternalException(String pDetailMessage)
  {
    super(pDetailMessage);
  }

  /**
   * Create a new internal exception with a throwable cause.
   *
   * @param pCause the cause of the exception
   */
  public OJInternalException(Throwable pCause)
  {
    super(pCause);
  }

  /**
   * Create a new internal exception with a detail message and a throwable cause.
   *
   * @param pDetailMessage a detailed message describing the cause of the exception
   * @param pCause         the cause of the exception
   */
  public OJInternalException(String pDetailMessage, Throwable pCause)
  {
    super(pDetailMessage, pCause);
  }
}
