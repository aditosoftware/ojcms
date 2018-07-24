package de.adito.beans.persistence.datastores.sql.builder.format;

import de.adito.beans.persistence.datastores.sql.builder.definition.EDatabaseType;

/**
 * Enumerates all types of statement formatters.
 * A type is able to create an instance of a {@link StatementFormatter}.
 * This way is the only way to get instances of a formatter.
 *
 * @author Simon Danner, 19.07.2018
 */
public enum EFormatter
{
  CREATE("TABLE"), INSERT("INTO"), SELECT("FROM"), UPDATE(""), DELETE("FROM");

  private final String tableNamePrefix;

  /**
   * Creates a new formatter type.
   *
   * @param pTableNamePrefix the table name prefix of this type (e.g. 'FROM' for 'SELECT', or 'INTO' for 'INSERT')
   */
  EFormatter(String pTableNamePrefix)
  {
    tableNamePrefix = pTableNamePrefix;
  }

  /**
   * Creates an instance of a statement formatter from this type.
   *
   * @param pDatabaseType the database type to use for the statements
   * @param pIdColumnName a global id column name for the statements
   * @return an instance a of statement formatter
   */
  public StatementFormatter create(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return new StatementFormatter(pDatabaseType, pIdColumnName, name(), tableNamePrefix);
  }
}
