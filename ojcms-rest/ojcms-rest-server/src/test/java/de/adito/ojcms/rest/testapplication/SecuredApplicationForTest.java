package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.persistence.AdditionalPersist;
import de.adito.ojcms.rest.application.OJSecuredRestApplication;
import de.adito.ojcms.rest.auth.api.AuthenticationResponse;
import jakarta.ws.rs.ApplicationPath;

import java.util.Arrays;

import static de.adito.ojcms.rest.auth.api.RegistrationRequest.*;
import static de.adito.ojcms.rest.testapplication.RegistrationRequestForTest.USER_ROLE;

/**
 * A secured REST application for testing purposes
 *
 * @author Simon Danner, 22.04.2020
 */
@AdditionalPersist(beanType = SomeAdditionalTestBean.class, containerId = "ADDITIONAL")
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
    return new UserForTest(pRequest.getValue(USER_MAIL), pRequest.getValue(DISPLAY_NAME), pRequest.getValue(USER_ROLE));
  }

  @Override
  public AuthenticationResponse createAuthenticationResponse(String pToken, String pNextPassword, UserForTest pAuthenticatedUser)
  {
    return new AuthenticationResponse(pToken, pNextPassword);
  }
}
