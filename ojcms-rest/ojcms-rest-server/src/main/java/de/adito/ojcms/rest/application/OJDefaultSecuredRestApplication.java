package de.adito.ojcms.rest.application;

import de.adito.ojcms.rest.auth.api.*;
import de.adito.ojcms.rest.security.SecureBoundary;
import de.adito.ojcms.rest.security.user.OJUser;

/**
 * Base class for a secured OJCMS REST application.
 * This class is a default variant of {@link OJSecuredRestApplication} that uses {@link SecureBoundary} as boundary annotation.
 * See base class for detailed information.
 *
 * @param <USER>                 the type of the user for the application
 * @param <REGISTRATION_REQUEST> the registration request for the user management
 * @param <AUTH_RESPONSE>        the authentication response for the client
 * @author Simon Danner, 19.04.2020
 */
public abstract class OJDefaultSecuredRestApplication<USER extends OJUser, REGISTRATION_REQUEST extends IRegistrationRequest,
    AUTH_RESPONSE extends AuthenticationResponse>
    extends OJSecuredRestApplication<SecureBoundary, USER, REGISTRATION_REQUEST, AUTH_RESPONSE>
{
  /**
   * Initializes the secured application with the user type and all REST resources to register.
   *
   * @param pUserType      the type of the user for the application
   * @param pRestResources the REST resources to register
   */
  protected OJDefaultSecuredRestApplication(Class<USER> pUserType, Class<?>... pRestResources)
  {
    super(SecureBoundary.class, pUserType, pRestResources);
  }
}
