package de.adito.beans.core.util.exceptions;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;

/**
 * Thrown, when a bean field is not existing on a certain bean.
 *
 * @author Simon Danner, 17.08.2017
 */
public class BeanFieldDoesNotExistException extends RuntimeException
{
  public BeanFieldDoesNotExistException(IBean<?> pBean, IField<?> pField)
  {
    super("bean: " + pBean.getClass().getSimpleName() + " field: " + pField.getName());
  }
}
