package de.adito.ojcms.beans.exceptions.copy;

import de.adito.ojcms.beans.literals.fields.IField;

/**
 * Exception for a bean field that does not support a copy mechanism for its value.
 *
 * @author Simon Danner, 14.04.2018
 */
public class BeanCopyNotSupportedException extends Exception
{
  /**
   * Creates a new unsupported copy exception.
   *
   * @param pBeanField the bean field, that isn't able to create a copy
   * @param pCause     the cause for the copy failure
   */
  public BeanCopyNotSupportedException(IField<?> pBeanField, Throwable pCause)
  {
    super(_createErrorMessage(pBeanField), pCause);
  }

  /**
   * Creates a new unsupported copy exception.
   *
   * @param pBeanField the bean field, that isn't able to create a copy
   */
  public BeanCopyNotSupportedException(IField<?> pBeanField)
  {
    super(_createErrorMessage(pBeanField));
  }

  /**
   * The error message for this exception based on a bean field.
   *
   * @param pBeanField the bean field that isn't able to create a copy
   * @return the error message
   */
  private static String _createErrorMessage(IField<?> pBeanField)
  {
    return "It is not possible to create a value copy of the following bean field: " + pBeanField.getClass().getName() + " value type: " +
        pBeanField.getDataType().getName() + " There may be no copy mechanism provided by the field and no default constructor for " +
        "the value type. As a workaround you may use custom field copy functions. For further information take a look at the bean interface.";
  }
}
