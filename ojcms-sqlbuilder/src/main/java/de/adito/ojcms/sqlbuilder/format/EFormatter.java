package de.adito.ojcms.sqlbuilder.format;

import de.adito.ojcms.sqlbuilder.platform.IDatabasePlatform;

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
   * @param pPlatform     the database platform to use for the statements
   * @param pIdColumnName a global id column name for the statements
   * @return an instance a of statement formatter
   */
  public StatementFormatter create(IDatabasePlatform pPlatform, String pIdColumnName)
  {
    return new StatementFormatter(pPlatform, pIdColumnName, name(), tableNamePrefix);
  }
}
