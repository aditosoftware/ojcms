package de.adito.beans.core.util.exceptions;

/**
 * Thrown during an exception in the bean flatten process.
 * May indicate an illegal state of the bean.
 *
 * @author Simon Danner, 28.06.2017
 */
public class BeanFlattenException extends RuntimeException
{
  public BeanFlattenException()
  {
  }

  public BeanFlattenException(String pMessage)
  {
    super(pMessage);
  }
}
