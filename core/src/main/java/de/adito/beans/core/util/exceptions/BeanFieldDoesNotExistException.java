package de.adito.beans.core.util.exceptions;

import de.adito.beans.core.*;

/**
 * Thrown, when a bean field is not existing on a certain bean.
 *
 * @author Simon Danner, 17.08.2017
 */
public class BeanFieldDoesNotExistException extends RuntimeException
{
  public BeanFieldDoesNotExistException(IBean<?> pBean, IField<?> pField)
  {
    super("Missing bean field: bean-type: " + pBean.getClass().getSimpleName() + " field: " + pField.getName() + ". Consider field filters!");
  }

  public BeanFieldDoesNotExistException(IField<?> pField)
  {
    super("Missing bean field: " + pField.getName() + ". Consider field filters!");
  }
}
