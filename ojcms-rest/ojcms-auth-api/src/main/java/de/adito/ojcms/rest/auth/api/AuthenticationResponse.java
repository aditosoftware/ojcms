package de.adito.ojcms.rest.auth.api;

/**
 * Base class for the response of an authentication.
 * Contains the JWT for the HTTP requests and the password for the next authentication.
 * An user of the framework might extend this class to add more information.
 *
 * @author Simon Danner, 25.10.2019
 */
public class AuthenticationResponse
{
  private final String token;
  private final String nextPassword;

  public AuthenticationResponse(String pToken, String pNextPassword)
  {
    token = pToken;
    nextPassword = pNextPassword;
  }

  /**
   * The JWT for future HTTP requests.
   */
  public String getToken()
  {
    return token;
  }

  /**
   * The password for the next authentication.
   */
  public String getNextPassword()
  {
    return nextPassword;
  }
}
