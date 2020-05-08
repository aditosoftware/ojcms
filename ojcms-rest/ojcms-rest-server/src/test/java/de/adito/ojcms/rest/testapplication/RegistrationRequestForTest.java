package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.rest.auth.api.RegistrationRequest;

/**
 * The registration request for testing purposes adding an user role.
 *
 * @author Simon Danner, 22.04.2020
 */
public class RegistrationRequestForTest extends RegistrationRequest
{
  private final EUserRoleForTest userRole;

  public RegistrationRequestForTest(String pUserMail, String pDisplayName, EUserRoleForTest pUserRole)
  {
    super(pUserMail, pDisplayName);
    userRole = pUserRole;
  }

  public EUserRoleForTest getUserRole()
  {
    return userRole;
  }
}
