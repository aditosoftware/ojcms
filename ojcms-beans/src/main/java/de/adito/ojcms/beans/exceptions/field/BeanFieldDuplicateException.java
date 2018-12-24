package de.adito.ojcms.beans.exceptions.field;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.fields.IField;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown, if a bean declares fields with equal names.
 *
 * @author Simon Danner, 23.12.2018
 */
public class BeanFieldDuplicateException extends OJRuntimeException
{
  /**
   * Creates a duplicate exception for multiple conflicting fields.
   *
   * @param pDuplicateFields all duplicate fields
   */
  public BeanFieldDuplicateException(List<IField<?>> pDuplicateFields)
  {
    super("A bean cannot define a field twice! duplicates: " + pDuplicateFields.stream()
        .map(IField::getName)
        .collect(Collectors.joining(", ")));
  }

  /**
   * Creates a duplicate exception for a certain field name.
   *
   * @param pFieldName the duplicated field name
   */
  public BeanFieldDuplicateException(String pFieldName)
  {
    super("A field with the name '" + pFieldName + "' is already existing!");
  }
}
