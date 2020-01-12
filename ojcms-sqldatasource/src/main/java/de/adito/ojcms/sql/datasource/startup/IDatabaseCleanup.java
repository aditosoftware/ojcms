package de.adito.ojcms.sql.datasource.startup;

import de.adito.ojcms.sqlbuilder.OJSQLBuilder;

/**
 * Defines any database cleanup that should be performed on application startup.
 *
 * @author Simon Danner, 02.01.2020
 */
@FunctionalInterface
public interface IDatabaseCleanup
{
  /**
   * Performs the database cleanup on application startup
   *
   * @param pBuilder a SQL builder to execute statements
   */
  void cleanup(OJSQLBuilder pBuilder);
}
