package de.adito.ojcms.sqlbuilder.platform;

/**
 * Database platform definition for embedded types.
 *
 * @author Simon Danner, 02.11.2019
 * @see IDatabasePlatform
 */
public interface IEmbeddedDatabasePlatform extends IDatabasePlatform
{
  /**
   * Default database name for embedded platforms.
   */
  String EMBEDDED_DB_NAME = "OJCMS_EMBEDDED";

  /**
   * Provides the JDBC connection string for the database platform.
   *
   * @param pInMemory <tt>true</tt> if the embedded database should be in memory
   * @return a JDBC connection string
   */
  String getConnectionString(boolean pInMemory);
}
