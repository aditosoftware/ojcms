package de.adito.ojcms.rest.security.user.exceptions;

/**
 * Indicates that an user could not be found.
 *
 * @author Simon Danner, 30.11.2019
 */
public class UserNotFoundException extends Exception
{
  /**
   * Initializes the exception.
   *
   * @param pMail the mail address of the user that could not have been found
   */
  public UserNotFoundException(String pMail)
  {
    super("User with mail address " + pMail + " does not exist!");
  }
}
