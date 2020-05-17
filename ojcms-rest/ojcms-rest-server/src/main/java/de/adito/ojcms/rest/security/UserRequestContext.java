package de.adito.ojcms.rest.security;

import de.adito.ojcms.rest.security.user.OJUser;

import javax.enterprise.context.RequestScoped;
import java.util.Objects;

/**
 * Provides the user context of a REST request.
 *
 * @author Simon Danner, 11.05.2020
 */
@RequestScoped
public class UserRequestContext
{
  private OJUser requestingUser;

  /**
   * The requesting user of the currently active REST call.
   *
   * @param <USER> the generic type of the user of the application
   * @return the requesting user
   * @throws IllegalStateException if no user has been set for the call
   */
  public <USER extends OJUser> USER getRequestingUser()
  {
    if (requestingUser == null)
      throw new IllegalStateException("No user set for active request!");

    //noinspection unchecked
    return (USER) requestingUser;
  }

  /**
   * Sets the requesting user for the currently active REST call.
   *
   * @param pRequestingUser the requesting user to set for the call
   */
  void setAuthenticatedUserForRequest(OJUser pRequestingUser)
  {
    requestingUser = Objects.requireNonNull(pRequestingUser);
  }
}
