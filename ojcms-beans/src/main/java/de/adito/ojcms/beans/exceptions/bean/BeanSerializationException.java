package de.adito.ojcms.beans.exceptions.bean;

/**
 * Exception for bean serialization problems.
 *
 * @author Simon Danner, 25.05.2018
 */
public class BeanSerializationException extends RuntimeException
{
  /**
   * Creates a new serialization exception.
   *
   * @param pMessage a detailed error message
   */
  public BeanSerializationException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Creates a new serialization exception.
   *
   * @param pMessage   a detailed error message
   * @param pThrowable the cause of the exception
   */
  public BeanSerializationException(String pMessage, Throwable pThrowable)
  {
    super(pMessage, pThrowable);
  }
}
