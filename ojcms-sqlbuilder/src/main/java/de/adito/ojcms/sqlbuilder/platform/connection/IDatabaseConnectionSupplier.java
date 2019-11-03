package de.adito.ojcms.sqlbuilder.platform.connection;

import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;

import java.sql.Connection;

/**
 * Supplies new database connections based on specific {@link IDatabasePlatform} definitions.
 *
 * @author Simon Danner, 03.11.2019
 */
public interface IDatabaseConnectionSupplier
{
  /**
   * Creates a new database connection.
   *
   * @return the newly created connection
   */
  Connection createNewConnection();

  /**
   * The database platform the new connections are for.
   *
   * @return the associated database platform
   */
  IDatabasePlatform getPlatform();
}
