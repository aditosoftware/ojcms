package de.adito.ojcms.beans.exceptions.bean;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.OJRuntimeException;

/**
 * Exception for a non declared bean type. (Might be transformed or another representation only using the interface)
 * A declared bean type is necessary to get information about fields.
 *
 * @author Simon Danner, 23.12.2018
 */
public class NoDeclaredBeanTypeException extends OJRuntimeException
{
  public NoDeclaredBeanTypeException(Class<? extends IBean> pBeanType, String pDetailCause)
  {
    super(pBeanType.getName() + " is not a declared bean type! Bean field information cannot be retrieved!" + pDetailCause);
  }
}
