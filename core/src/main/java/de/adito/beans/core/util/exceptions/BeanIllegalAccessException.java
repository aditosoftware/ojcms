package de.adito.beans.core.util.exceptions;

import de.adito.beans.core.*;

/**
 * Thrown, if private bean fields are accessed publicly.
 *
 * @author Simon Danner, 29.01.2018
 */
public class BeanIllegalAccessException extends RuntimeException
{
  public BeanIllegalAccessException(IBean<?> pBean, IField<?> pField)
  {
    super("The field " + pField.getName() + " of the bean " + pBean.getClass().getSimpleName() + " is only allowed to be accessed privately!");
  }
}
