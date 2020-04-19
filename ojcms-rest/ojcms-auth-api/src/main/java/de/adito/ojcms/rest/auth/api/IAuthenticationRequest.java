package de.adito.ojcms.rest.auth.api;

import java.io.Serializable;

/**
 * Defines an authentication request for the authentication REST resource.
 *
 * @author Simon Danner, 07.12.2019
 */
public interface IAuthenticationRequest extends Serializable
{
  /**
   * The mail address of the user to authenticate.
   */
  String getUserMail();

  /**
   * The password of the user to authenticate.
   */
  String getPassword();
}
