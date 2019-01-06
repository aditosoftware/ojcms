package de.adito.ojcms.beans.literals;

/**
 * Identifier for any additional information of a bean member.
 *
 * @param <INFO> the data type of the information
 * @author Simon Danner, 01.06.2017
 * @see IMemberLiteral
 */
public interface IAdditionalMemberInfo<INFO>
{
  /**
   * The data type of the additional information.
   *
   * @return the data type
   */
  Class<INFO> getDataType();
}
