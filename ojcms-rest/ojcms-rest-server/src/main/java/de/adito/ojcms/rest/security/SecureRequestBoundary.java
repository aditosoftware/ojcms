package de.adito.ojcms.rest.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.adito.ojcms.beans.IBeanContainer;
import de.adito.ojcms.cdi.ICdiControl;
import de.adito.ojcms.rest.security.user.OJUser;
import de.adito.ojcms.rest.security.util.JWTUtil;
import de.adito.ojcms.transactions.util.TransactionalExecution;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import javax.annotation.Priority;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * The HTTP request filter to prevent unauthorized access for REST resources that are annotated by the application's secure boundary
 * annotation type (like {@link SecureBoundary}. The filter also excludes requests that do not apply to
 * {@link IBoundaryValidation#isUserAllowedToCrossBoundary(Annotation, OJUser)}.
 *
 * @param <BOUNDARY> the type of the boundary annotation of the application
 * @param <USER>     the type of the users of the application
 * @author Simon Danner, 20.09.2019
 */
@Priority(Priorities.AUTHENTICATION)
@Provider
public class SecureRequestBoundary<BOUNDARY extends Annotation, USER extends OJUser> implements ContainerRequestFilter
{
  private static final int TOKEN_PREFIX_LENGTH = "Bearer".length();

  @Context
  private ResourceInfo resourceInfo;

  private final IBoundaryValidation<BOUNDARY, USER> boundaryValidation;
  private final Class<USER> userType;
  private IBeanContainer<USER> users;
  private UserRequestContext userRequestContext;
  private TransactionalExecution transactionalExecution;

  public SecureRequestBoundary(IBoundaryValidation<BOUNDARY, USER> pBoundaryValidation, Class<USER> pUserType)
  {
    boundaryValidation = pBoundaryValidation;
    userType = pUserType;
  }

  @Override
  public void filter(ContainerRequestContext pRequestContext)
  {
    try
    {
      final Method boundaryMethod = resourceInfo.getResourceMethod();
      if (!boundaryMethod.isAnnotationPresent(boundaryValidation.getBoundaryType()))
        return;

      _initializeCdiComponents();

      final String token = _retrieveTokenFromHeader(pRequestContext);
      final DecodedJWT decoded = JWTUtil.decodeJwt(token);
      final String userMail = decoded.getClaim(JWTUtil.USER_MAIL_CLAIM).asString();
      final Optional<USER> authenticatedUser = transactionalExecution.resolveResult(() -> users.findOneByFieldValue(OJUser.MAIL, userMail));

      if (!authenticatedUser.isPresent())
      {
        pRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        return;
      }

      final BOUNDARY boundaryAnnotation = boundaryMethod.getAnnotation(boundaryValidation.getBoundaryType());
      if (!(boundaryValidation).isUserAllowedToCrossBoundary(boundaryAnnotation, authenticatedUser.get()))
        pRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());

      userRequestContext.setAuthenticatedUserForRequest(authenticatedUser.get());
    }
    catch (JWTVerificationException pE)
    {
      pRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }

  /**
   * Lazily initializes CDI components. Cannot be done while construction because CDI container is not booted then.
   */
  private void _initializeCdiComponents()
  {
    if (users == null)
      users = ICdiControl.current().createInjected(new ParameterizedTypeImpl(IBeanContainer.class, userType));
    if (userRequestContext == null)
      userRequestContext = ICdiControl.current().createInjected(UserRequestContext.class);
    if (transactionalExecution == null)
      transactionalExecution = ICdiControl.current().createInjected(TransactionalExecution.class);
  }

  /**
   * Retrieves the string based JWT from the HTTP header.
   *
   * @param pRequestContext the HTTP request context
   * @return the string based JWT from the header
   * @throws JWTVerificationException if the header does not contain a token
   */
  private static String _retrieveTokenFromHeader(ContainerRequestContext pRequestContext) throws JWTVerificationException
  {
    final String authHeader = pRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

    if (authHeader == null)
      throw new JWTVerificationException("No authorization token in header!");

    return authHeader.substring(TOKEN_PREFIX_LENGTH).trim();
  }
}
