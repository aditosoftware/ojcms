package de.adito.beans.core.exceptions;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.IField;

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
