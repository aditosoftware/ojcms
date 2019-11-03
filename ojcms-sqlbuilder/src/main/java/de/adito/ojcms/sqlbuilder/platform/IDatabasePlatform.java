package de.adito.ojcms.sqlbuilder.platform;

import de.adito.ojcms.sqlbuilder.definition.column.IColumnType;

/**
 * Defines a database platform (MYSQL, ORACLE, DERBY etc).
 * The main purpose of such a platform is to create a connection to the specific external or embedded database.
 * It also provides some platform dependent information like column type mapping and driver names.
 *
 * @author Simon Danner, 02.11.2019
 */
public interface IDatabasePlatform
{
  /**
   * An identifying name for the database platform.
   *
   * @return the name of the platform
   */
  String getPlatformName();

  /**
   * The prefix for system database tables.
   *
   * @return system table prefix
   */
  String getSystemTablePrefix();

  /**
   * Initializes the driver for the specific database platform.
   */
  void initDriver();

  /**
   * A column type in its statement format.
   *
   * @param pColumnType the column type
   * @return the definition of a column as string for this database platform
   */
  String columnTypeToStatementFormat(IColumnType pColumnType);
}
