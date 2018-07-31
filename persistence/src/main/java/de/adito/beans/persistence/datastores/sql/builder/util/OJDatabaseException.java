package de.adito.beans.persistence.datastores.sql.builder.util;

/**
 * Exception class regarding SQL/database errors within the OJ database statement builder framework.
 *
 * @author Simon Danner, 20.02.2018
 */
public class OJDatabaseException extends RuntimeException
{
  /**
   * Creates a new database exception.
   *
   * @param pMessage a detailed error message
   */
  public OJDatabaseException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Creates a new database exception.
   *
   * @param pCause the cause of the exception
   */
  public OJDatabaseException(Throwable pCause)
  {
    super(pCause);
  }

  /**
   * Creates a new database exception.
   *
   * @param pCause   the cause of the exception
   * @param pMessage a detailed error message
   */
  public OJDatabaseException(Throwable pCause, String pMessage)
  {
    super(pMessage, pCause);
  }

  /**
   * Creates a new database exception.
   *
   * @param pQuery the query which led to the exception
   * @param pCause the cause of the exception
   */
  public OJDatabaseException(String pQuery, Throwable pCause)
  {
    super("SQL-Error while executing query '" + pQuery + "'", pCause);
  }
}
