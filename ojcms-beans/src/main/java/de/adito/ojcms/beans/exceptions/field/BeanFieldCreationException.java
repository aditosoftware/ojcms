package de.adito.ojcms.beans.exceptions.field;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.OJRuntimeException;

/**
 * Thrown, if it is not possible to create a bean field via reflection.
 *
 * @author Simon Danner, 23.12.2018
 */
public class BeanFieldCreationException extends OJRuntimeException
{
  /**
   * Creates the exception with the associated bean type where the field should have been created.
   *
   * @param pBeanType the bean type where the field should have been created
   */
  public BeanFieldCreationException(Class<? extends IBean> pBeanType)
  {
    super("Unable to create field. There are no static field or all of them are initialized already. " +
              "bean-type: " + pBeanType.getName() + ". Check the class type given to the field factory! " +
              "It must be the same as the type containing the bean field to create.");
  }

  /**
   * Creates the exception with a detail message and a cause.
   *
   * @param pDetailMessage a detailed message describing the cause of the exception
   * @param pCause         the cause of the exception
   */
  public BeanFieldCreationException(String pDetailMessage, Throwable pCause)
  {
    super(pDetailMessage, pCause);
  }
}
