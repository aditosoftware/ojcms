package de.adito.ojcms.beans.exceptions.bean;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.literals.fields.IField;

/**
 * Thrown, if a null value is set or requested for a bean field annotated by {@link de.adito.ojcms.beans.annotations.NeverNull}.
 *
 * @author Simon Danner, 25.12.2018
 */
public class NullValueForbiddenException extends OJRuntimeException
{
  /**
   * Creates a new exception.
   *
   * @param pField the field that does not allow null values
   */
  public NullValueForbiddenException(IField<?> pField)
  {
    super(pField + " does not allow null values!");
  }
}
