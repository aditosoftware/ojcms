package de.adito.ojcms.rest.security.user.exceptions;

/**
 * Indicates that an invalid mail address has been used.
 *
 * @author Simon Danner, 30.11.2019
 */
public class BadMailAddressException extends Exception
{
  public BadMailAddressException(String pMailAddress)
  {
    super(pMailAddress + " is not a valid email address!");
  }
}
