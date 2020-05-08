package de.adito.ojcms.rest.auth.api;

import java.io.Serializable;

/**
 * Defines an user registration request.
 *
 * @author Simon Danner, 07.12.2019
 */
public class RegistrationRequest implements Serializable
{
  private final String userMail;
  private final String displayName;

  public RegistrationRequest(String pUserMail, String pDisplayName)
  {
    userMail = pUserMail;
    displayName = pDisplayName;
  }

  /**
   * The mail address of the user to register.
   */
  public String getUserMail()
  {
    return userMail;
  }

  /**
   * The display name of the user to register.
   */
  public String getDisplayName()
  {
    return displayName;
  }
}
