package de.adito.ojcms.beans.fields.util;

import de.adito.ojcms.beans.fields.IField;

/**
 * Any type that is based or holds a bean field.
 *
 * @param <VALUE> the value type of the bean field
 * @author Simon Danner, 07.12.2018
 */
public interface IBeanFieldBased<VALUE>
{
  /**
   * The bean field this instance is based on.
   *
   * @return a bean field
   */
  IField<VALUE> getBeanField();
}
