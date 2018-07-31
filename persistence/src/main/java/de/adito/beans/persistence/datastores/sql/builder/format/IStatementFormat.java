package de.adito.beans.persistence.datastores.sql.builder.format;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;

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
   * @param pDatabaseType the database type used for the statement
   * @param pIdColumnName the global name of the id column
   * @return a string representing the definition
   */
  String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName);
}
