package de.adito.ojcms.beans.fields.util;

/**
 * Identifier for any additional information of a bean field.
 *
 * @param <INFO> the data type of the information
 * @author Simon Danner, 01.06.2017
 * @see de.adito.ojcms.beans.fields.IField
 */
public interface IAdditionalFieldInfo<INFO>
{
  /**
   * The data type of this additional information.
   *
   * @return the data type
   */
  Class<INFO> getDataType();
}
