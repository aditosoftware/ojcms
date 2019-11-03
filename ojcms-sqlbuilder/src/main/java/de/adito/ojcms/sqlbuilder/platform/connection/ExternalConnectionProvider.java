package de.adito.ojcms.sqlbuilder.platform.connection;

import de.adito.ojcms.sqlbuilder.platform.*;
import de.adito.ojcms.sqlbuilder.util.OJDatabaseException;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

import static de.adito.ojcms.utils.StringUtility.requireNotEmpty;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link IDatabaseConnectionSupplier} for external databases. It requires external information about host, port, etc.
 *
 * @author Simon Danner, 03.11.2019
 */
public final class ExternalConnectionProvider implements IDatabaseConnectionSupplier
{
  private final IExternalDatabasePlatform platform;
  private final String host;
  private final int port;
  private final String databaseName;
  @Nullable
  private final String username, password;

  /**
   * Creates a new connection provider for an external database.
   *
   * @param pPlatform     the database platform to use
   * @param pHost         the host address of the database to connect to
   * @param pPort         the port of the database to connect to
   * @param pDatabaseName the name of the database to connect to
   */
  public ExternalConnectionProvider(IExternalDatabasePlatform pPlatform, String pHost, int pPort, String pDatabaseName)
  {
    this(pPlatform, pHost, pPort, pDatabaseName, null, null);
  }

  /**
   * Creates a new connection provider for an external database.
   *
   * @param pPlatform     the database platform to use
   * @param pHost         the host address of the database to connect to
   * @param pPort         the port of the database to connect to
   * @param pDatabaseName the name of the database to connect to
   * @param pUsername     an optional username to use for the connection
   * @param pPassword     an optional password to use for the connection
   */
  public ExternalConnectionProvider(IExternalDatabasePlatform pPlatform, String pHost, int pPort, String pDatabaseName,
                                    @Nullable String pUsername, @Nullable String pPassword)
  {
    if (pPort < 0 || pPort > 65535)
      throw new IllegalArgumentException("The port has to be between 0 and 65535!");

    platform = requireNonNull(pPlatform);
    host = requireNotEmpty(pHost, "host");
    port = pPort;
    databaseName = requireNotEmpty(pDatabaseName, "database name");
    username = pUsername;
    password = pPassword;
  }

  @Override
  public Connection createNewConnection()
  {
    final String connectionString = platform.getConnectionString(host, port, databaseName);
    try
    {
      return username == null || password == null ? DriverManager.getConnection(connectionString) :
          DriverManager.getConnection(connectionString, username, password);
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE, "Unable to connect to external database! platform = " + platform.getPlatformName() +
          " host = " + host + " port = " + port);
    }
  }

  @Override
  public IDatabasePlatform getPlatform()
  {
    return platform;
  }
}
