package de.adito.ojcms.beans.exceptions.bean;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.fields.IField;

/**
 * Thrown, if private bean fields are accessed publicly.
 *
 * @author Simon Danner, 29.01.2018
 */
public class BeanIllegalAccessException extends OJRuntimeException
{
  public BeanIllegalAccessException(IBean<?> pBean, IField<?> pField)
  {
    super("The field " + pField.getName() + " of the bean " + pBean.getClass().getSimpleName() + " is only allowed to be accessed privately!");
  }
}
