package de.adito.ojcms.beans.exceptions.bean;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.literals.fields.IField;

/**
 * Thrown, if the value of a final bean field is set multiple times.
 *
 * @author Simon Danner, 16.10.2019
 */
public class FieldIsFinalException extends OJRuntimeException
{
  public FieldIsFinalException(IField<?> pField)
  {
    super("Some value for field " + pField.getName() + " has already been set! Setting another value is forbidden!");
  }
}
