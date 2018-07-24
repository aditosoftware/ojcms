package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.definition.column.*;
import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.util.*;
import java.util.function.*;

/**
 * Enumerates all possible database types for this statement builder framework.
 *
 * @author Simon Danner, 29.04.2018
 */
public enum EDatabaseType
{
  /**
   * Default column values:
   * - VARCHAR: 255
   * - CHAR: 1
   */
  DERBY("org.apache.derby.jdbc.ClientDriver", "SYS",
        (pHost, pPort, pDatabaseName) -> "jdbc:derby://" + pHost + ":" + pPort + "/" + pDatabaseName + ";create=true",
        _derbyColumnMapping());

  private final String driverName;
  private final String systemTablesPrefix;
  private final ConnectionStringBuilder connectionStringBuilder;
  private final Map<EColumnType, Function<IColumnType, String>> columnDefinitions;

  /**
   * Creates a new database type.
   *
   * @param pDriverName              the fully qualified JDBC driver name
   * @param pSystemTablesPrefix      the prefix of system tables
   * @param pConnectionStringBuilder a JDBC connection string builder
   * @param pColumnDefinitions       a map of column definitions for this specific database type
   */
  EDatabaseType(String pDriverName, String pSystemTablesPrefix, ConnectionStringBuilder pConnectionStringBuilder,
                Map<EColumnType, Function<IColumnType, String>> pColumnDefinitions)
  {
    driverName = pDriverName;
    systemTablesPrefix = pSystemTablesPrefix;
    connectionStringBuilder = pConnectionStringBuilder;
    columnDefinitions = Collections.unmodifiableMap(pColumnDefinitions);
  }

  /**
   * Initializes the driver of the database type.
   */
  public void initDriver()
  {
    try
    {
      Class.forName(driverName);
    }
    catch (ClassNotFoundException pE)
    {
      throw new RuntimeException("Driver '" + driverName + "' not found!", pE);
    }
  }

  /**
   * The prefix of system tables of this database type.
   *
   * @return a system table prefix
   */
  public String getSystemTablesPrefix()
  {
    return systemTablesPrefix;
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
    return connectionStringBuilder.build(pHost, pPort, pDatabaseName);
  }

  /**
   * A column type in its statement format.
   *
   * @param pColumnType the column type
   * @return the definition of a column as string for this database type
   */
  public String columnTypeToStatementFormat(IColumnType pColumnType)
  {
    EColumnType type = pColumnType.getType();
    if (!columnDefinitions.containsKey(type))
      throw new OJDatabaseException("No column definition given for " + type.name() + ". (database type: " + name() + ")");
    return columnDefinitions.get(type).apply(pColumnType);
  }

  /**
   * The column name mapping for derby database systems.
   *
   * @return a map to resolve a column definition by the column type
   */
  private static Map<EColumnType, Function<IColumnType, String>> _derbyColumnMapping()
  {
    return _createColumnMapping(pMap -> {
      pMap.put(EColumnType.LONG, pType -> "BIGINT");
      pMap.put(EColumnType.BLOB, pType -> "BLOB");
      pMap.put(EColumnType.CHAR, pType -> "CHAR(1)");
      pMap.put(EColumnType.DATE, pType -> "DATE");
      pMap.put(EColumnType.DATETIME, pType -> "DATETIME");
      pMap.put(EColumnType.DOUBLE, pType -> "DOUBLE");
      pMap.put(EColumnType.FLOAT, pType -> "FLOAT");
      pMap.put(EColumnType.INT, pType -> "INTEGER");
      pMap.put(EColumnType.SHORT, pType -> "SHORT");
      pMap.put(EColumnType.TIME, pType -> "TIME");
      pMap.put(EColumnType.STRING, pType -> "VARCHAR(" + (pType.hasLength() ? pType.getLength() : 255) + ")");
    });
  }

  /**
   * Creates an empty column name mapping to insert entries afterwards.
   *
   * @param pMapConsumer a consumer of the created empty map to insert entries
   * @return the filled column mapping
   */
  private static Map<EColumnType, Function<IColumnType, String>> _createColumnMapping(Consumer<Map<EColumnType,
      Function<IColumnType, String>>> pMapConsumer)
  {
    Map<EColumnType, Function<IColumnType, String>> map = new HashMap<>();
    pMapConsumer.accept(map);
    return map;
  }

  /**
   * Functional definition of a connection string builder.
   * It creates a JDBC connection string from the host address, port and database name.
   */
  @FunctionalInterface
  public interface ConnectionStringBuilder
  {
    /**
     * Builds a JDBC connection string.
     *
     * @param pHost         the host to connect to
     * @param pPort         the port to connect to
     * @param pDatabaseName the database name to connect to
     * @return a JDBC connection string
     */
    String build(String pHost, int pPort, String pDatabaseName);
  }
}
