package de.adito.ojcms.beans.exceptions.copy;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;

/**
 * Exception for errors while creating a copy of a bean.
 *
 * @author Simon Danner, 10.04.2018
 */
public class BeanCopyException extends OJRuntimeException
{
  /**
   * Creates a copy exception based on a copy unsupported exception relating to a bean field that is not able to copy its value.
   *
   * @param pUnsupportedException the unsupported exception
   */
  public BeanCopyException(BeanCopyNotSupportedException pUnsupportedException)
  {
    super(pUnsupportedException);
  }
}
