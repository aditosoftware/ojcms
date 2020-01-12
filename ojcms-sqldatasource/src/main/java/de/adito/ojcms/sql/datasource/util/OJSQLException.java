package de.adito.ojcms.sql.datasource.util;

/**
 * Indicates an unexpected SQL-related failure.
 *
 * @author Simon Danner, 31.12.2019
 */
public class OJSQLException extends RuntimeException
{
  /**
   * Initializes the exception with a detailed error message.
   *
   * @param pMessage the cause as message
   */
  public OJSQLException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Initializes the exception with a detailed error message and a throwable cause.
   *
   * @param pMessage the cause as message
   * @param pCause   the throwable cause that led to the exception
   */
  public OJSQLException(String pMessage, Throwable pCause)
  {
    super(pMessage, pCause);
  }
}
