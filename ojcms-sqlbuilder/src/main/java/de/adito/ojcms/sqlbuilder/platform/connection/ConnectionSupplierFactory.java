package de.adito.ojcms.sqlbuilder.platform.connection;

import de.adito.ojcms.sqlbuilder.platform.*;
import org.jetbrains.annotations.Nullable;

/**
 * A factory to create {@link IDatabasePlatform} dependent {@link IDatabaseConnectionSupplier} instances.
 *
 * @author Simon Danner, 03.11.2019
 */
@SuppressWarnings("MethodMayBeStatic")
public final class ConnectionSupplierFactory
{
  private final boolean autoCommit;

  /**
   * Initializes the factory.
   *
   * @param pAutoCommit <tt>true</tt> if auto commit should be activated for every executed statement
   */
  public ConnectionSupplierFactory(boolean pAutoCommit)
  {
    autoCommit = pAutoCommit;
  }

  /**
   * Creates a database connection supplier for an external database system.
   * This method uses no username and password for the database connection.
   *
   * @param pPlatform     the external database platform
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @return the created database connection supplier
   */
  public IDatabaseConnectionSupplier forExternalDatabase(EExternalDatabasePlatform pPlatform, String pHost, int pPort, String pDatabaseName)
  {
    return forExternalDatabase(pPlatform, pHost, pPort, pDatabaseName, null, null);
  }

  /**
   * Creates a database connection supplier for an external database system.
   *
   * @param pPlatform     the external database platform
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the database name to connect to
   * @param pUserName     an optional user name for the connection
   * @param pPassword     an optional password for the connection
   * @return he created database connection supplier
   */
  public IDatabaseConnectionSupplier forExternalDatabase(EExternalDatabasePlatform pPlatform, String pHost, int pPort, String pDatabaseName,
                                                         @Nullable String pUserName, @Nullable String pPassword)
  {
    return new ExternalConnectionProvider(pPlatform.platform(), pHost, pPort, pDatabaseName, pUserName, pPassword, autoCommit);
  }

  /**
   * Creates a database connection supplier for an embedded database system.
   * This method uses an internal embedded database with default values for database name etc.
   *
   * @param pEmbeddedPlatform the embedded platform of choice
   * @param pInMemory         <tt>true</tt> if the embedded database should only live in memory
   * @return the created connection supplier for the embedded database
   */
  public IDatabaseConnectionSupplier forEmbeddedDatabase(EEmbeddedDatabasePlatform pEmbeddedPlatform, boolean pInMemory)
  {
    return new EmbeddedConnectionProvider(pEmbeddedPlatform.getPlatform(), pInMemory, autoCommit);
  }
}
