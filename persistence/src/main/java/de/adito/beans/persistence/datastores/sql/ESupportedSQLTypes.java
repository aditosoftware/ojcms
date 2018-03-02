package de.adito.beans.persistence.datastores.sql;

/**
 * All supported SQL database types of this persistence framework.
 *
 * @author Simon Danner, 18.02.2018
 */
public enum ESupportedSQLTypes
{
  DERBY("org.apache.derby.jdbc.ClientDriver", "jdbc:derby://{host}:{port}/{dbName};create=true");

  private static final String HOST = "{host}";
  private static final String PORT = "{port}";
  private static final String DB_NAME = "{dbName}";

  private final String driverName;
  private final String connectionString;

  /**
   * Creates a new supported type.
   *
   * @param pDriverName       the fully qualified JDBC driver name
   * @param pConnectionString the JDBC connection string (it uses variables declared above)
   */
  ESupportedSQLTypes(String pDriverName, String pConnectionString)
  {
    driverName = pDriverName;
    connectionString = pConnectionString;
  }

  /**
   * The type's JDBC driver name.
   */
  public String getDriverName()
  {
    return driverName;
  }

  /**
   * The connection string for this type.
   *
   * @param pHost         the host to connect to
   * @param pPort         the port to connect to
   * @param pDatabaseName the name of the database to connect to
   * @return the connection string
   */
  public String getConnectionString(String pHost, int pPort, String pDatabaseName)
  {
    String connection = connectionString;
    connection = connection.replace(HOST, pHost);
    connection = connection.replace(PORT, String.valueOf(pPort));
    connection = connection.replace(DB_NAME, pDatabaseName);
    return connection;
  }
}
