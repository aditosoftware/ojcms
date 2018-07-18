package de.adito.beans.persistence.datastores.sql.builder.definition.format;

/**
 * This definition can be presented in a database statement format.
 *
 * @author Simon Danner, 03.07.2018
 */
public interface IStatementFormat
{
  /**
   * The definition in its database statement format.
   *
   * @return a string representing the definition
   */
  String toStatementFormat();
}
