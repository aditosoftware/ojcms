package de.adito.beans.persistence.datastores.sql.builder.format;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;

import java.util.List;

/**
 * This definition can be presented in a prepared statement format.
 * Placeholders ("?") will be replaced by argument within the execution of the statement.
 *
 * @author Simon Danner, 03.07.2018
 */
public interface IPreparedStatementFormat extends IStatementFormat
{
  /**
   * The arguments for the prepared statement.
   *
   * @param pDatabaseType the database type used for the statement
   * @param pIdColumnName the global name of the id column
   * @return a list of column value tuples, that are used as arguments
   */
  List<IColumnValueTuple<?>> getArguments(EDatabaseType pDatabaseType, String pIdColumnName);
}
