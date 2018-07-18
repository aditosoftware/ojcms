package de.adito.beans.persistence.datastores.sql.builder.definition.format;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;

/**
 * This definition can be presented in a database statement format.
 * The definition is based on a database type.
 *
 * @author Simon Danner, 18.07.2018
 */
public interface ITypeStatementFormat
{
  /**
   * The definition in its database statement format.
   *
   * @param pDatabaseType the database type used for the statement
   * @return a string representing the definition
   */
  String toStatementFormat(EDatabaseType pDatabaseType);
}
