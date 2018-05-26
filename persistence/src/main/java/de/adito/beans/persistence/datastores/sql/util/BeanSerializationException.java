package de.adito.beans.persistence.datastores.sql.util;

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
   * @param pThrowable the cause for the exception
   */
  public BeanSerializationException(Throwable pThrowable)
  {
    super(pThrowable);
  }
}
