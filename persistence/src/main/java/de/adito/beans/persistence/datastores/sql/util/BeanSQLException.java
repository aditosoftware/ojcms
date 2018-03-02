package de.adito.beans.persistence.datastores.sql.util;

/**
 * Exception class regarding sql errors within the bean persistence context.
 *
 * @author Simon Danner, 20.02.2018
 */
public class BeanSQLException extends RuntimeException
{
  /**
   * Creates a new Exception.
   *
   * @param pMessage a detailed error message
   */
  public BeanSQLException(String pMessage)
  {
    super(pMessage);
  }

  /**
   * Creates a new Exception.
   *
   * @param pCause the cause of the exception
   */
  public BeanSQLException(Throwable pCause)
  {
    super(pCause);
  }

  /**
   * Creates a new Exception.
   *
   * @param pQuery the query which led to the exception
   * @param pCause the cause of the exception
   */
  public BeanSQLException(String pQuery, Throwable pCause)
  {
    super("SQL-Error while executing query '" + pQuery + "'", pCause);
  }
}
