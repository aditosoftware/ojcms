package de.adito.ojcms.sqlbuilder.serialization;

import java.sql.PreparedStatement;

/**
 * Represent a serial value that may be applied to a {@link PreparedStatement}.
 *
 * @author Simon Danner, 05.01.2020
 */
public interface ISerialValue
{
  /**
   * Applies the serial value to a {@link PreparedStatement} as parameter at a specific index.
   *
   * @param pStatement the statement to add the serial value to
   * @param pIndex     the index of the parameter
   */
  void applyToStatement(PreparedStatement pStatement, int pIndex);
}
