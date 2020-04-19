package de.adito.ojcms.rest.config;

/**
 * Indicates a miss configuration in the database config file.
 *
 * @author Simon Danner, 12.01.2020
 */
public class DatabaseConfigException extends RuntimeException
{
  public DatabaseConfigException(String pMessage)
  {
    super(pMessage);
  }

  public DatabaseConfigException(String pMessage, NumberFormatException pE)
  {
    super(pMessage);
  }
}
