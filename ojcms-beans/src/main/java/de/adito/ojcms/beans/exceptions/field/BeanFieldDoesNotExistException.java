package de.adito.ojcms.beans.exceptions.field;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.literals.fields.IField;

/**
 * Thrown, when a bean field is not existing at a certain bean.
 *
 * @author Simon Danner, 17.08.2017
 */
public class BeanFieldDoesNotExistException extends OJRuntimeException
{
  /**
   * Creates a new exception for a certain field on a specific bean.
   *
   * @param pBean  the bean missing the field
   * @param pField the missing field
   */
  public BeanFieldDoesNotExistException(IBean pBean, IField<?> pField)
  {
    super(
        "Missing bean field: bean-type: " + pBean.getClass().getSimpleName() + " field: " + pField.getName() + ". Consider field filters!");
  }

  /**
   * Creates a new exception for a certain field. The bean is not specified here.
   *
   * @param pField the missing field
   */
  public BeanFieldDoesNotExistException(IField<?> pField)
  {
    super("Missing bean field: " + pField.getName() + ". Consider field filters!");
  }

  /**
   * Creates a new exception for a certain field name on a specific bean.
   *
   * @param pBean      the bean missing the field
   * @param pFieldName the missing field's name
   */
  public BeanFieldDoesNotExistException(IBean pBean, String pFieldName)
  {
    super("A bean field with the name '" + pFieldName + "' is not existing at bean type " + pBean.getClass().getName());
  }
}
