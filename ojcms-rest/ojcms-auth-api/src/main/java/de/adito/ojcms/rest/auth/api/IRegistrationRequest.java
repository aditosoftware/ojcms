package de.adito.ojcms.rest.auth.api;

import java.io.Serializable;

/**
 * Defines an user registration request.
 *
 * @author Simon Danner, 07.12.2019
 */
public interface IRegistrationRequest extends Serializable
{
  /**
   * The mail address of the user to register.
   */
  String getUserMail();

  /**
   * The display name of the user to register.
   */
  String getDisplayName();
}
