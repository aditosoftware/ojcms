package de.adito.beans.core.fields.util;

import de.adito.beans.core.IField;

/**
 * Identifier for any additional information of a bean field.
 *
 * @param <TYPE> the data type of the information
 * @author Simon Danner, 01.06.2017
 * @see IField
 */
public interface IAdditionalFieldInfo<TYPE>
{
  /**
   * The data type of this additional information.
   *
   * @return the data type
   */
  Class<TYPE> getType();
}