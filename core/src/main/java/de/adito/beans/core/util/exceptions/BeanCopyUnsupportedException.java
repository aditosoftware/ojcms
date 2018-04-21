package de.adito.beans.core.util.exceptions;

import de.adito.beans.core.IField;

/**
 * Exception for a bean field, which does not support a copy mechanism for its value.
 *
 * @author Simon Danner, 14.04.2018
 */
public class BeanCopyUnsupportedException extends Exception
{
  public BeanCopyUnsupportedException(IField<?> pBeanField)
  {
    super("It is not possible to create a value copy of the following bean field: " + pBeanField.getClass().getName() + " value type: " +
              pBeanField.getType().getName() + " There may be no copy mechanism provided by the field and no default constructor for " +
              "the value type. As a workaround you may use custom field copy functions. For further information take a look at the bean interface.");
  }
}
