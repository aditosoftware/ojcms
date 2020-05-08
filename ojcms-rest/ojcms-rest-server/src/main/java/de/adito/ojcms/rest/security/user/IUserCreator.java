package de.adito.ojcms.rest.security.user;

import de.adito.ojcms.rest.auth.api.*;

/**
 * Defines how to create the user of the application based on a {@link RegistrationRequest}.
 * Furthermore defines how to create the generic {@link AuthenticationResponse} after a successful authentication.
 *
 * @param <USER>                 the generic user type of the application
 * @param <REGISTRATION_REQUEST> the generic type of user registration requests of the application
 * @param <AUTH_RESPONSE>        the generic type of authentication responses of the application
 * @author Simon Danner, 08.04.2020
 */
public interface IUserCreator<USER extends OJUser, REGISTRATION_REQUEST extends RegistrationRequest,
    AUTH_RESPONSE extends AuthenticationResponse>
{
  /**
   * Creates a new user from a registration request.
   *
   * @param pUserRegistrationRequest the registration request
   * @return the newly created user
   */
  USER createNewUser(REGISTRATION_REQUEST pUserRegistrationRequest);

  /**
   * Creates an authentication response for the client of a REST call.
   *
   * @param pToken             the JWT for the REST authentication
   * @param pNextPassword      the password for the next authentication
   * @param pAuthenticatedUser the authenticated user
   * @return the created authentication response
   */
  AUTH_RESPONSE createAuthenticationResponse(String pToken, String pNextPassword, USER pAuthenticatedUser);

  /**
   * The type of the users of the application.
   */
  Class<USER> getUserType();
}
