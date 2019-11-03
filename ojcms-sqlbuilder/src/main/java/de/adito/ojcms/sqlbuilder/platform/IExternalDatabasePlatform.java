package de.adito.ojcms.sqlbuilder.platform;

/**
 * Database platform definition for external systems.
 *
 * @author Simon Danner, 02.11.2019
 * @see IDatabasePlatform
 */
public interface IExternalDatabasePlatform extends IDatabasePlatform
{
  /**
   * Resolves a connection string for the external database.
   *
   * @param pHost         the host address of the external database
   * @param pPort         the port of the external database
   * @param pDatabaseName the name of the external database
   * @return the connection string for the external database
   */
  String getConnectionString(String pHost, int pPort, String pDatabaseName);
}
