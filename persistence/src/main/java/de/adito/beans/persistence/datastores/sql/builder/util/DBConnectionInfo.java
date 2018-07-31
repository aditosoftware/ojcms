package de.adito.beans.persistence.datastores.sql.builder.util;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.Objects;

/**
 * Several information for a database connection.
 *
 * @author Simon Danner, 23.05.2018
 */
public class DBConnectionInfo
{
  private final EDatabaseType databaseType;
  private final String host, databaseName;
  private final int port;
  @Nullable
  private final String username, password;

  /**
   * Creates a new database information instance.
   *
   * @param pDatabaseType the type of the database to connect to
   * @param pHost         the host address of the database to connect to
   * @param pPort         the port of the database to connect to
   * @param pDatabaseName the name of the database to connect to
   * @param pUsername     an optional username to use for the connection
   * @param pPassword     an optional password to use for the connection
   */
  public DBConnectionInfo(EDatabaseType pDatabaseType, String pHost, int pPort, String pDatabaseName, @Nullable String pUsername,
                          @Nullable String pPassword)
  {
    if (pPort < 0 || pPort > 65535)
      throw new IllegalArgumentException("The port has to be between 0 and 65535!");
    databaseType = Objects.requireNonNull(pDatabaseType);
    host = Objects.requireNonNull(pHost);
    port = pPort;
    username = pUsername;
    password = pPassword;
    databaseName = Objects.requireNonNull(pDatabaseName);
  }

  /**
   * The type of the database to connect to
   *
   * @return a database type
   */
  public EDatabaseType getDatabaseType()
  {
    return databaseType;
  }

  /**
   * The host address of the database to connect to.
   *
   * @return a host address
   */
  public String getHost()
  {
    return host;
  }

  /**
   * The port of the database to connect to.
   *
   * @return a port number
   */
  public int getPort()
  {
    return port;
  }

  /**
   * The name of the database to connect to.
   *
   * @return a database name
   */
  public String getDatabaseName()
  {
    return databaseName;
  }

  /**
   * An optional username to use for the connection.
   *
   * @return a username or null, if not necessary
   */
  @Nullable
  public String getUsername()
  {
    return username;
  }

  /**
   * An optional password to use for the connection.
   *
   * @return a password or null, if not necessary
   */
  @Nullable
  public String getPassword()
  {
    return password;
  }

  /**
   * Creates a JDBC connection string for this connection information.
   *
   * @return a JDBC connection string
   */
  public String getJDBCConnectionString()
  {
    return databaseType.getConnectionString(host, port, databaseName);
  }

  /**
   * Creates a database connection based on the given connection information.
   *
   * @return the database connection
   */
  public Connection createConnection()
  {
    final String connectionString = getJDBCConnectionString();
    try
    {
      return username == null || password == null ? DriverManager.getConnection(connectionString) :
          DriverManager.getConnection(connectionString, username, password);
    }
    catch (SQLException pE)
    {
      throw new OJDatabaseException(pE, "Unable to connect to the database! host = " + host + " port = " + port);
    }
  }
}
