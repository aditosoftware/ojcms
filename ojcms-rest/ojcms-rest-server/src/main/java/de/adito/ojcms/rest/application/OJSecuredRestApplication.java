package de.adito.ojcms.rest.application;

import de.adito.ojcms.persistence.Persist;
import de.adito.ojcms.rest.auth.api.*;
import de.adito.ojcms.rest.security.*;
import de.adito.ojcms.rest.security.user.*;
import de.adito.ojcms.rest.serialization.*;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.container.ContainerRequestFilter;

import java.lang.annotation.Annotation;

/**
 * Base class for a secured OJCMS REST application.
 * A secured application provides a JWT based security mechanism and user management.
 * Within this application class you must define the type of the user and the type of the boundary annotation for your REST interfaces.
 * <p>
 * Security mechanism: You are able to annotate JAX-RS REST methods that require authentication with the provided annotation type.
 * Use either {@link SecureBoundary} as default annotation type for just JWT based authentication or provide a custom one to add special
 * information like required user roles, for example. A custom annotation must be annotated with {@link jakarta.ws.rs.ext.Provider}.
 * Then, if the secured REST interface is called, a {@link ContainerRequestFilter} will check if the client is providing a valid JWT.
 * This class also allows you to define a method to validate the request based on the custom annotation. For example, you may check if the
 * authenticated user (trough JWT) has the required user role defined in the custom boundary annotation.
 * <p>
 * Trough the generic concept of this class you are able to define the user type based on {@link OJUser}. The concrete user type must be
 * annotated with {@link Persist}. Furthermore the registration request and the authentication response may be extended to provide
 * application specific user information.
 * <p>
 * Mark the implementing sub class with {@link ApplicationPath} to define the base path for your REST interfaces.
 * <p>
 * Hint: Use {@link OJDefaultSecuredRestApplication} if you want to use {@link SecureBoundary} as boundary annotation.
 *
 * @param <BOUNDARY>             the type of the boundary annotation to secure REST interfaces
 * @param <USER>                 the type of the user for the application
 * @param <REGISTRATION_REQUEST> the registration request for the user management
 * @param <AUTH_RESPONSE>        the authentication response for the client
 * @author Simon Danner, 07.04.2020
 */
public abstract class OJSecuredRestApplication<BOUNDARY extends Annotation, USER extends OJUser,
    REGISTRATION_REQUEST extends RegistrationRequest, AUTH_RESPONSE extends AuthenticationResponse>
    extends OJRestApplication implements IUserCreator<USER, REGISTRATION_REQUEST, AUTH_RESPONSE>, IBoundaryValidation<BOUNDARY, USER>
{
  private final Class<BOUNDARY> boundaryAnnotationType;
  private final Class<USER> userType;

  /**
   * Initializes the secured application with the boundary annotation type, the user type and all REST resources to register.
   *
   * @param pBoundaryAnnotationType the type of the boundary annotation
   * @param pUserType               the type of the user for the application
   * @param pRestResources          the REST resources to register
   */
  protected OJSecuredRestApplication(Class<BOUNDARY> pBoundaryAnnotationType, Class<USER> pUserType,
      Class<REGISTRATION_REQUEST> pRegistrationRequestType, Class<?>... pRestResources)
  {
    super(pRestResources);
    boundaryAnnotationType = pBoundaryAnnotationType;
    userType = _verifyUserType(pUserType);

    final UserService<USER, REGISTRATION_REQUEST, AUTH_RESPONSE> userService = new UserService<>(this);
    providerAndResourceTypes.add(AuthenticationRestService.class);
    providerAndResourceInstances.add(new AuthenticationRestService<>(userService));
    providerAndResourceInstances.add(new SecureRequestBoundary<>(this, pUserType));

    providerAndResourceTypes.remove(BeanSerializationProvider.class);
    providerAndResourceInstances.add(new SecurityBeanSerializationProvider<>(pRegistrationRequestType));
  }

  @Override
  public Class<BOUNDARY> getBoundaryType()
  {
    return boundaryAnnotationType;
  }

  @Override
  public Class<USER> getUserType()
  {
    return userType;
  }

  /**
   * Verifies a given user type by checking if the class is annotated with {@link Persist}.
   *
   * @param pUserType the type of the user to verify
   * @return the verified user type
   */
  private Class<USER> _verifyUserType(Class<USER> pUserType)
  {
    if (!pUserType.isAnnotationPresent(Persist.class))
      throw new IllegalArgumentException("The user type for the rest application must be annotation with @Persist!");
    return pUserType;
  }
}
