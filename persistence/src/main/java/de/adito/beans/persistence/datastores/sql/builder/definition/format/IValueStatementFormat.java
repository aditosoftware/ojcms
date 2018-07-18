package de.adito.beans.persistence.datastores.sql.builder.definition.format;

import de.adito.beans.persistence.datastores.sql.builder.definition.IValueSerializer;

/**
 * This definition can be presented in a database statement format.
 * The definition may use a value, that has to be serialized.
 *
 * @author Simon Danner, 18.07.2018
 */
public interface IValueStatementFormat
{
  /**
   * The definition in its database statement format.
   *
   * @param pSerializer the value serializer
   * @return a string representing the definition
   */
  String toStatementFormat(IValueSerializer pSerializer);
}
