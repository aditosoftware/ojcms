package de.adito.ojcms.persistence;

import de.adito.ojcms.beans.IBean;

/**
 * General persistence exception for the framework.
 *
 * @author Simon Danner, 23.12.2018
 */
public class OJPersistenceException extends RuntimeException
{
  /**
   * Creates a new persistence exception with a detail message describing the cause of the exception.
   *
   * @param pDetailMessage a detailed message describing the cause of the exception.
   */
  public OJPersistenceException(String pDetailMessage)
  {
    super(pDetailMessage);
  }

  /**
   * Creates a new persistence exception for a bean type that is expected to be annotated as persistent bean.
   *
   * @param pNonPersistentBean the type of the bean not annotated as persistent bean
   */
  public OJPersistenceException(Class<? extends IBean> pNonPersistentBean)
  {
    super("The bean type '" + pNonPersistentBean.getName() + "' is excepted to be annotated as persistent bean!");
  }
}
