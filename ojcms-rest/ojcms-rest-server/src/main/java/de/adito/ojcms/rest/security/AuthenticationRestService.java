package de.adito.ojcms.rest.security;

import de.adito.ojcms.rest.application.OJSecuredRestApplication;
import de.adito.ojcms.rest.auth.api.*;
import de.adito.ojcms.rest.security.user.OJUser;
import de.adito.ojcms.rest.security.user.exceptions.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

/**
 * REST interface for user registration and authentication.
 * This resource will be registered as singleton instance at the {@link OJSecuredRestApplication}.
 * The generic types of this class will be defined by the user of the framework to specify the user, registration request and
 * authentication response types.
 *
 * @param <USER>                 the generic type of the user to authenticate
 * @param <REGISTRATION_REQUEST> the generic type of registration requests for the clients
 * @param <AUTH_RESPONSE>>       the generic type of authentication responses for the clients
 * @author Simon Danner, 22.09.2019
 */
@Path("/authentication")
public class AuthenticationRestService<USER extends OJUser, REGISTRATION_REQUEST extends RegistrationRequest,
    AUTH_RESPONSE extends AuthenticationResponse>
{
  private final UserService<USER, REGISTRATION_REQUEST, AUTH_RESPONSE> userService;

  public AuthenticationRestService(UserService<USER, REGISTRATION_REQUEST, AUTH_RESPONSE> pUserService)
  {
    userService = pUserService;
  }

  @Path("/auth")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response authUser(AuthenticationRequest pAuthenticationRequest)
  {
    try
    {
      final AUTH_RESPONSE authResponse = userService.authenticateUser(pAuthenticationRequest);
      return Response.ok(authResponse).build();
    }
    catch (BadCredentialsException pE)
    {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }

  @Path("/register")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response registerUser(REGISTRATION_REQUEST pRegistrationRequest)
  {
    try
    {
      final AUTH_RESPONSE authResponse = userService.registerNewUser(pRegistrationRequest);
      return Response.ok(authResponse).build();
    }
    catch (UserAlreadyExistsException | BadMailAddressException pE)
    {
      return _exceptionResponse(pE);
    }
  }

  @Path("/requestCode")
  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  public Response requestRestoreCode(String pUserMail)
  {
    try
    {
      userService.requestRestoreCodeByMail(pUserMail);
      return Response.noContent().build();
    }
    catch (UserNotFoundException | RestoreCodeAlreadyActive pE)
    {
      return _exceptionResponse(pE);
    }
  }

  @Path("/restore")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response restoreUser(RestoreAuthenticationRequest pRestoreAuthRequest)
  {
    try
    {
      final AUTH_RESPONSE authResponse = userService.restoreAuthentication(pRestoreAuthRequest);
      return Response.ok(authResponse).build();
    }
    catch (BadRestoreCodeException | UserNotFoundException pE)
    {
      return _exceptionResponse(pE);
    }
  }

  /**
   * Creates a HTTP response that indicates a BAD_REQUEST due to the error message of an {@link Exception}.
   *
   * @param pException the exception causing the bad request
   * @return the HTTP response for the bad request
   */
  private Response _exceptionResponse(Exception pException)
  {
    return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), pException.getMessage()).build();
  }
}
