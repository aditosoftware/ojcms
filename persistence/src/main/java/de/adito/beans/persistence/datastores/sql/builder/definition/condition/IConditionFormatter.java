package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.IValueSerializer;

/**
 * Any condition information, that can be presented in a database statement format.
 *
 * @author Simon Danner, 09.06.2018
 */
public interface IConditionFormatter
{
  /**
   * The condition information in a database statement format.
   *
   * @param pSerializer a serializer to convert to value to a serial string format
   * @return the statement format
   */
  String toStatementFormat(IValueSerializer pSerializer);
}
