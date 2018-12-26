package de.adito.ojcms.beans.exceptions.bean;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.exceptions.OJRuntimeException;

/**
 * Thrown, if a bean type is not able to provide creation events via {@link BeanCreationEvents}.
 *
 * @author Simon Danner, 26.12.2018
 */
public class BeanCreationNotObservableException extends OJRuntimeException
{
  /**
   * Creates a new exception for a specific bean type
   *
   * @param pBeanType the bean type that is not observable for creation events
   */
  public BeanCreationNotObservableException(Class<? extends IBean> pBeanType)
  {
    super("It is not possible to observe creation events for bean type " + pBeanType.getName());
  }

  /**
   * Creates a new exception with a detail message
   *
   * @param pMessage a detailed message describing the cause of the exception
   */
  public BeanCreationNotObservableException(String pMessage)
  {
    super(pMessage);
  }
}
