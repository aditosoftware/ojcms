package de.adito.ojcms.rest.security.user.exceptions;

/**
 * Indicates that the authentication of an user failed due to bad credentials.
 *
 * @author Simon Danner, 27.09.2019
 */
public class BadCredentialsException extends Exception
{
  public BadCredentialsException(String pMail)
  {
    super("Authentication failed for user " + pMail + " due to bad credentials!");
  }
}
