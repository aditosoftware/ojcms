package de.adito.beans.persistence.datastores.sql.builder.definition;

import de.adito.beans.persistence.datastores.sql.builder.util.OJDatabaseException;

import java.util.*;
import java.util.function.*;

/**
 * Enumerates all possible database types for this query builder framework.
 *
 * @author Simon Danner, 29.04.2018
 */
public enum EDatabaseType
{
  DERBY("org.apache.derby.jdbc.ClientDriver",
        (pHost, pPort, pDatabaseName) -> "jdbc:derby://" + pHost + ":" + pPort + "/" + pDatabaseName + ";create=true",
        _derbyColumnMapping());

  private final String driverName;
  private final ConnectionStringBuilder connectionStringBuilder;
  private final Map<EColumnType, Function<Integer, String>> columnDefinitions;

  /**
   * Creates a new database type.
   *
   * @param pDriverName              the fully qualified JDBC driver name
   * @param pConnectionStringBuilder a JDBC connection string builder
   * @param pColumnDefinitions       a map of column definitions for this specific database type
   */
  EDatabaseType(String pDriverName, ConnectionStringBuilder pConnectionStringBuilder,
                Map<EColumnType, Function<Integer, String>> pColumnDefinitions)
  {
    driverName = pDriverName;
    connectionStringBuilder = pConnectionStringBuilder;
    columnDefinitions = Collections.unmodifiableMap(pColumnDefinitions);
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
    return connectionStringBuilder.build(pHost, pPort, pDatabaseName);
  }

  /**
   * A column definition for this database type.
   *
   * @param pColumnType the column type
   * @return the definition of a column as string for this database type
   */
  public String getColumnDefinition(EColumnType pColumnType)
  {
    return getColumnDefinition(pColumnType, -1);
  }

  /**
   * A column definition with a size information for this database type.
   *
   * @param pColumnType the column type
   * @param pSize       the size of the column
   * @return the definition of a column as string for this database type
   */
  public String getColumnDefinition(EColumnType pColumnType, int pSize)
  {
    if (!columnDefinitions.containsKey(pColumnType))
      throw new OJDatabaseException("No column definition given for " + pColumnType.name() + ". (database type: " + name() + ")");
    return columnDefinitions.get(pColumnType).apply(pSize);
  }

  /**
   * The column name mapping for derby database systems.
   *
   * @return a map to resolve a column definition by the column type
   */
  private static Map<EColumnType, Function<Integer, String>> _derbyColumnMapping()
  {
    return _createColumnMapping(pMap -> {
      pMap.put(EColumnType.LONG, pSize -> "BIGINT");
      pMap.put(EColumnType.BLOB, pSize -> "BLOB");
      pMap.put(EColumnType.SINGLE_CHAR, pSize -> "CHAR(1)");
      pMap.put(EColumnType.DATE, pSize -> "DATE");
      pMap.put(EColumnType.DATETIME, pSize -> "DATETIME");
      pMap.put(EColumnType.DOUBLE, pSize -> "DOUBLE");
      pMap.put(EColumnType.FLOAT, pSize -> "FLOAT");
      pMap.put(EColumnType.INT, pSize -> "INTEGER");
      pMap.put(EColumnType.SHORT, pSize -> "SHORT");
      pMap.put(EColumnType.TIME, pSize -> "TIME");
      pMap.put(EColumnType.STRING, pSize -> "VARCHAR(" + pSize + ")");
    });
  }

  /**
   * Creates an empty column name mapping to insert entries afterwards.
   *
   * @param pMapConsumer a consumer of the created empty map to insert entries
   * @return the filled column name mapping
   */
  private static Map<EColumnType, Function<Integer, String>> _createColumnMapping(Consumer<Map<EColumnType, Function<Integer, String>>> pMapConsumer)
  {
    Map<EColumnType, Function<Integer, String>> map = new HashMap<>();
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
