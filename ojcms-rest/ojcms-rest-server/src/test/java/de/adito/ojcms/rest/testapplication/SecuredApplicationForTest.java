package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.rest.application.OJSecuredRestApplication;
import de.adito.ojcms.rest.auth.api.AuthenticationResponse;
import jakarta.ws.rs.ApplicationPath;

import java.util.Arrays;

/**
 * A secured REST application for testing purposes
 *
 * @author Simon Danner, 22.04.2020
 */
@ApplicationPath("/")
public class SecuredApplicationForTest
    extends OJSecuredRestApplication<TestBoundary, UserForTest, RegistrationRequestForTest, AuthenticationResponse>
{
  public SecuredApplicationForTest()
  {
    super(TestBoundary.class, UserForTest.class, RegistrationRequestForTest.class, RestResourceForTest.class);
  }

  @Override
  public boolean isUserAllowedToCrossBoundary(TestBoundary pBoundary, UserForTest pUser)
  {
    if (pBoundary.requiresOneOfTheseRoles().length == 0)
      return true;

    return Arrays.asList(pBoundary.requiresOneOfTheseRoles()).contains(pUser.getValue(UserForTest.USER_ROLE));
  }

  @Override
  public UserForTest createNewUser(RegistrationRequestForTest pRequest)
  {
    return new UserForTest(pRequest.getUserMail(), pRequest.getDisplayName(), pRequest.getUserRole());
  }

  @Override
  public AuthenticationResponse createAuthenticationResponse(String pToken, String pNextPassword, UserForTest pAuthenticatedUser)
  {
    return new AuthenticationResponse(pToken, pNextPassword);
  }
}
