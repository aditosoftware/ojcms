package de.adito.ojcms.rest.auth.api;

import java.io.Serializable;

/**
 * Defines a request to restore authentication for an user with a code.
 *
 * @author Simon Danner, 07.12.2019
 */
public final class RestoreAuthenticationRequest implements Serializable
{
  private final String userMail;
  private final String restoreCode;

  /**
   * Creates a new restore request.
   *
   * @param pUserMail    the mail address of the user to restore authentication for
   * @param pRestoreCode the string based restore code
   */
  public RestoreAuthenticationRequest(String pUserMail, String pRestoreCode)
  {
    userMail = pUserMail;
    restoreCode = pRestoreCode;
  }

  /**
   * The mail address of the user to restore authentication for.
   */
  public String getUserMail()
  {
    return userMail;
  }

  /**
   * The string based restore code.
   */
  public String getRestoreCode()
  {
    return restoreCode;
  }
}
