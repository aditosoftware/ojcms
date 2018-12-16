package de.adito.ojcms.sqlbuilder.format;

import de.adito.ojcms.sqlbuilder.definition.IColumnValueTuple;

import java.util.List;

/**
 * This type can be presented in a prepared statement format.
 * Placeholders ("?") will be replaced by arguments within the execution of the statement.
 *
 * @author Simon Danner, 03.07.2018
 */
public interface IPreparedStatementFormat extends IStatementFormat
{
  /**
   * The arguments for the prepared statement.
   *
   * @param pIdColumnName the global name of the id column
   * @return a list of column value tuples, that are used as arguments
   */
  List<IColumnValueTuple<?>> getArguments(String pIdColumnName);
}
