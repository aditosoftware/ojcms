package de.adito.ojcms.rest.security;

import de.adito.ojcms.rest.security.user.OJUser;

import java.lang.annotation.Annotation;

/**
 * Defines the secure boundary and a mechanism to authorize access to the REST interfaces of the application.
 *
 * @param <BOUNDARY> the type of the boundary annotation
 * @param <USER>     the user type of the application
 * @author Simon Danner, 08.04.2020
 */
public interface IBoundaryValidation<BOUNDARY extends Annotation, USER extends OJUser>
{
  /**
   * The type of the secure boundary annotation. Use {@link SecureBoundary} if only JWT based.
   * You may define custom annotations to define boundaries that require a special user role etc.
   * A custom boundary annotation must be annotated with {@link jakarta.ws.rs.ext.Provider}.
   *
   * @return the type of the boundary annotation
   */
  Class<BOUNDARY> getBoundaryType();

  /**
   * Evaluates if a user is allowed to cross the secure boundary and perform the requested REST call.
   *
   * @param pBoundary the boundary annotation of the called REST resource
   * @param pUser     the user to validate the access rights for
   * @return <tt>true</tt> if the user is allowed to cross the boundary
   */
  boolean isUserAllowedToCrossBoundary(BOUNDARY pBoundary, USER pUser);
}
