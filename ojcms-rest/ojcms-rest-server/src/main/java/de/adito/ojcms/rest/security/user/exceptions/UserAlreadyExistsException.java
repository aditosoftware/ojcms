package de.adito.ojcms.rest.security.user.exceptions;

/**
 * Indicates that an user (mail address) already exists.
 *
 * @author Simon Danner, 13.10.2019
 */
public class UserAlreadyExistsException extends Exception
{
  /**
   * Initializes the exception with the mail address that already exists.
   */
  public UserAlreadyExistsException(String pMail)
  {
    super("The user " + pMail + " already exists!");
  }
}
