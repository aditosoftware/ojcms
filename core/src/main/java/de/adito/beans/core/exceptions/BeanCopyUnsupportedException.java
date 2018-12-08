package de.adito.beans.core.exceptions;

import de.adito.beans.core.fields.IField;
import org.jetbrains.annotations.NotNull;

/**
 * Exception for a bean field, which does not support a copy mechanism for its value.
 *
 * @author Simon Danner, 14.04.2018
 */
public class BeanCopyUnsupportedException extends Exception
{
  /**
   * Creates a new unsupported copy exception.
   *
   * @param pBeanField the bean field, that isn't able to create a copy
   * @param pCause     the cause for the copy failure
   */
  public BeanCopyUnsupportedException(IField<?> pBeanField, Throwable pCause)
  {
    super(_createErrorMessage(pBeanField), pCause);
  }

  /**
   * Creates a new unsupported copy exception.
   *
   * @param pBeanField the bean field, that isn't able to create a copy
   */
  public BeanCopyUnsupportedException(IField<?> pBeanField)
  {
    super(_createErrorMessage(pBeanField));
  }

  /**
   * The error message for this exception based on a bean field
   *
   * @param pBeanField the bean field, that isn't able to create a copy
   * @return the error message
   */
  @NotNull
  private static String _createErrorMessage(IField<?> pBeanField)
  {
    return "It is not possible to create a value copy of the following bean field: " + pBeanField.getClass().getName() + " value type: " +
        pBeanField.getDataType().getName() + " There may be no copy mechanism provided by the field and no default constructor for " +
        "the value type. As a workaround you may use custom field copy functions. For further information take a look at the bean interface.";
  }
}
