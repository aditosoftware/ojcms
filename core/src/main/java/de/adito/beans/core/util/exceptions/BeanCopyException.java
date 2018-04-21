package de.adito.beans.core.util.exceptions;

/**
 * Exception for errors while creating a copy of a bean.
 *
 * @author Simon Danner, 10.04.2018
 */
public class BeanCopyException extends RuntimeException
{
  /**
   * Default constructor, no detailed message.
   */
  public BeanCopyException()
  {
  }

  /**
   * Creates a copy exception with a detailed error description.
   *
   * @param pMessage the detailed message causing the exception
   */
  public BeanCopyException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Creates a copy exception based on an unsupported exception relating to a bean field, that is not able to copy its value.
   *
   * @param pUnsupportedException the unsupported exception
   */
  public BeanCopyException(BeanCopyUnsupportedException pUnsupportedException)
  {
    super(pUnsupportedException);
  }
}
