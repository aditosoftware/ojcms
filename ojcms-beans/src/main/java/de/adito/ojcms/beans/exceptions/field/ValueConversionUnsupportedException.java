package de.adito.ojcms.beans.exceptions.field;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.fields.IField;

/**
 * Thrown, if a specific value conversion is not supported for bean field.
 *
 * @author Simon Danner, 23.12.2018
 */
public class ValueConversionUnsupportedException extends OJRuntimeException
{
  /**
   * Creates a new conversion unsupported exception.
   *
   * @param pField         the field that is unable to convert to a certain type
   * @param pTypeToConvert the failing data type to convert to or from
   */
  public ValueConversionUnsupportedException(IField<?> pField, Class<?> pTypeToConvert)
  {
    super("The field " + pField.getName() + " is not able to convert a value of type " + pTypeToConvert.getName());
  }
}
