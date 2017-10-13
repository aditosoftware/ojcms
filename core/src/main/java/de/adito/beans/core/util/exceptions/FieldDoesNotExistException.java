package de.adito.beans.core.util.exceptions;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;

/**
 * Beschreibt eine Exception, welche auftritt, wenn ein Bean-Feld bei einer bestimmten Bean nicht existiert
 *
 * @author s.danner, 17.08.2017
 */
public class FieldDoesNotExistException extends RuntimeException
{
  public FieldDoesNotExistException(IBean<?> pBean, IField<?> pField)
  {
    super("bean: " + pBean.getClass().getSimpleName() + " field: " + pField.getName());
  }
}
