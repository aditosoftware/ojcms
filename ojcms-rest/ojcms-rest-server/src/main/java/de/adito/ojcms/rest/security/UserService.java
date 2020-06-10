package de.adito.ojcms.rest.security;


import de.adito.ojcms.beans.IBeanContainer;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
import de.adito.ojcms.cdi.ICdiControl;
import de.adito.ojcms.rest.auth.api.*;
import de.adito.ojcms.rest.auth.util.SharedUtils;
import de.adito.ojcms.rest.security.user.*;
import de.adito.ojcms.rest.security.user.exceptions.*;
import de.adito.ojcms.rest.security.util.JWTUtil;
import de.adito.ojcms.transactions.util.TransactionalExecution;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static de.adito.ojcms.rest.auth.api.AuthenticationRequest.PASSWORD;
import static de.adito.ojcms.rest.auth.api.RegistrationRequest.USER_MAIL;
import static de.adito.ojcms.rest.auth.api.RestoreAuthenticationRequest.RESTORE_CODE;
import static de.adito.ojcms.rest.auth.util.SharedUtils.VALID_EMAIL_ADDRESS_REGEX;

/**
 * The internal service to authenticate, register and restore users.
 *
 * @param <USER>                 the type of the users of the application
 * @param <REGISTRATION_REQUEST> the type of the registration requests of the application
 * @param <AUTH_RESPONSE>        the type of the authentication responses of the application
 * @author Simon Danner, 30.09.2019
 */
public class UserService<USER extends OJUser, REGISTRATION_REQUEST extends RegistrationRequest,
    AUTH_RESPONSE extends AuthenticationResponse>
{
  private final IUserCreator<USER, REGISTRATION_REQUEST, AUTH_RESPONSE> userCreator;
  private IBeanContainer<USER> users;
  private RestoreCodeMailSender mailSender;
  private TransactionalExecution transactionalExecution;

  public UserService(IUserCreator<USER, REGISTRATION_REQUEST, AUTH_RESPONSE> pUserCreator)
  {
    userCreator = pUserCreator;
  }

  /**
   * Authenticates an user by an {@link AuthenticationRequest} within a new transaction.
   *
   * @param pRequest the user authentication request
   * @return the successful authentication response
   * @throws BadCredentialsException if the credentials in the request are not correct
   */
  AUTH_RESPONSE authenticateUser(AuthenticationRequest pRequest) throws BadCredentialsException
  {
    return _transactionalExecution().resolveResultThrowing(() ->
    {
      final String userMail = pRequest.getValue(AuthenticationRequest.USER_MAIL);
      final String password = pRequest.getValue(PASSWORD);

      final USER authenticatedUser =
          _users().findOneByFieldValues(new FieldValueTuple<>(OJUser.MAIL, userMail), new FieldValueTuple<>(OJUser.PASSWORD, password)) //
              .orElseThrow(() -> new BadCredentialsException(userMail));

      authenticatedUser.generateNewPassword();
      return _createAuthResponse(authenticatedUser);
    });
  }

  /**
   * Registers a new user within a new transaction.
   *
   * @param pRequest the registration request
   * @return the (automatic) authentication response after the registration
   * @throws UserAlreadyExistsException if the user (mail address) already exists
   * @throws BadMailAddressException    if the given mail address is not valid
   */
  AUTH_RESPONSE registerNewUser(REGISTRATION_REQUEST pRequest) throws UserAlreadyExistsException, BadMailAddressException
  {
    return _transactionalExecution().<AUTH_RESPONSE, BadMailAddressException, UserAlreadyExistsException>resolveResultTwoThrowing(() ->
    {
      final String email = pRequest.getValue(USER_MAIL);

      if (!SharedUtils.validatePattern(VALID_EMAIL_ADDRESS_REGEX, email))
        throw new BadMailAddressException(email);

      if (_users().findOneByFieldValue(OJUser.MAIL, email).isPresent())
        throw new UserAlreadyExistsException(email);

      final USER newUser = userCreator.createNewUser(pRequest);
      _users().addBean(newUser);

      return _createAuthResponse(newUser);
    });
  }

  /**
   * Requests an authentication restore code for an user by mail.
   *
   * @param pUserMail the mail address of the user to send the restore code
   * @throws UserNotFoundException if there's no user for the given mail address
   */
  void requestRestoreCodeByMail(String pUserMail) throws UserNotFoundException
  {
    _transactionalExecution().justRunThrowing(() ->
    {
      final OJUser user = _users().findOneByFieldValue(OJUser.MAIL, pUserMail) //
          .orElseThrow(() -> new UserNotFoundException(pUserMail));

      user.generateRestoreCode();
      _mailSender().sendRestoreCodeMail(user);
    });
  }

  /**
   * Restores the authentication for a user by providing a restore code within a {@link RestoreAuthenticationRequest}.
   *
   * @param pRestoreAuthRequest the request containing the user mail address and the restore code
   * @return the authentication response for the client
   * @throws UserNotFoundException   if there's no user for the given mail address
   * @throws BadRestoreCodeException if the restore code is not valid
   */
  AUTH_RESPONSE restoreAuthentication(RestoreAuthenticationRequest pRestoreAuthRequest)
      throws UserNotFoundException, BadRestoreCodeException
  {
    return _transactionalExecution().<AUTH_RESPONSE, UserNotFoundException, BadRestoreCodeException>resolveResultTwoThrowing(() ->
    {
      final USER user = _users().findOneByFieldValue(OJUser.MAIL, pRestoreAuthRequest.getValue(AuthenticationRequest.USER_MAIL)) //
          .orElseThrow(() -> new UserNotFoundException(pRestoreAuthRequest.getValue(AuthenticationRequest.USER_MAIL)));

      user.validateAndResetRestoreCode(pRestoreAuthRequest.getValue(RESTORE_CODE));
      user.generateNewPassword();

      return _createAuthResponse(user);
    });
  }

  /**
   * Creates an authentication response for an user.
   */
  private AUTH_RESPONSE _createAuthResponse(USER pUser)
  {
    final String jwt = JWTUtil.createJwtForUser(pUser);
    final String nextPassword = pUser.getValue(OJUser.PASSWORD);
    return userCreator.createAuthenticationResponse(jwt, nextPassword, pUser);
  }

  /**
   * Provides the user bean container.
   * If the field is not initialized, it will be via {@link ICdiControl#createInjected(Type, Annotation...)}.
   *
   * @return the user bean container
   */
  private IBeanContainer<USER> _users()
  {
    if (users == null)
      users = ICdiControl.current().createInjected(new ParameterizedTypeImpl(IBeanContainer.class, userCreator.getUserType()));
    return users;
  }

  /**
   * Provides the mail sender for restore codes.
   * If the field is not initialized, it will be via {@link ICdiControl#createInjected(Type, Annotation...)}.
   *
   * @return the mail sender for restore codes
   */
  private RestoreCodeMailSender _mailSender()
  {
    if (mailSender == null)
      mailSender = ICdiControl.current().createInjected(RestoreCodeMailSender.class);
    return mailSender;
  }

  /**
   * Provides the transactional execution helper.
   * If the field is not initialized, it will be via {@link ICdiControl#createInjected(Type, Annotation...)}.
   *
   * @return the transactional execution helper
   */
  private TransactionalExecution _transactionalExecution()
  {
    if (transactionalExecution == null)
      transactionalExecution = ICdiControl.current().createInjected(TransactionalExecution.class);
    return transactionalExecution;
  }
}
