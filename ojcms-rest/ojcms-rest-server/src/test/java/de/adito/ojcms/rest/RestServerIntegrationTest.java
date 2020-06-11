package de.adito.ojcms.rest;

import de.adito.ojcms.rest.auth.api.AuthenticationResponse;
import de.adito.ojcms.rest.serialization.GSONSerializationProvider;
import de.adito.ojcms.rest.testapplication.*;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.*;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.util.UUID;

import static de.adito.ojcms.rest.auth.api.AuthenticationResponse.TOKEN;
import static de.adito.ojcms.rest.testapplication.EUserRoleForTest.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the REST server.
 * Uses {@link SecuredApplicationForTest} as example test application.
 * This test only boots the CDI container once instead of for every test case.
 *
 * @author Simon Danner, 22.04.2020
 */
public class RestServerIntegrationTest extends JerseyTest
{
  private static final String USER_DISPLAY_NAME = "Test User";
  private static TestContainerProxy cachedContainer;
  private static Client cachedClient;

  @BeforeEach
  public void before() throws Exception
  {
    super.setUp(); //For JUnit 5 compatibility
  }

  @AfterAll
  public static void closeContainerAndClient()
  {
    cachedContainer.delegate.stop();
    closeIfNotNull(cachedClient);
  }

  @Override
  protected Client getClient()
  {
    if (cachedClient == null)
      cachedClient = super.getClient();
    return cachedClient;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() throws TestContainerException
  {
    final TestContainerFactory original = RestServerIntegrationTest.super.getTestContainerFactory();

    return (pURI, pDeploymentContext) ->
    {
      if (cachedContainer == null)
      {
        final TestContainer testContainer = original.create(pURI, pDeploymentContext);
        testContainer.start();
        cachedContainer = new TestContainerProxy(testContainer);
      }
      return cachedContainer;
    };
  }

  @Override
  protected Application configure()
  {
    return new SecuredApplicationForTest();
  }

  @Override
  protected void configureClient(ClientConfig config)
  {
    config.register(GSONSerializationProvider.class);
  }

  @Test
  public void testGetSomeSecretValue()
  {
    final String token = registerUserForAuthToken(ROLE1);
    final String result = target("/someSecuredService/secret") //
        .request() //
        .header("Authorization", "Bearer " + token) //
        .get(String.class);

    assertEquals("42", result);
  }

  @Test
  public void testGetSomeSecretValue_WrongRole()
  {
    final String token = registerUserForAuthToken(ROLE2);
    final Response response = target("/someSecuredService/secret") //
        .request() //
        .header("Authorization", "Bearer " + token) //
        .get();

    assertEquals(401, response.getStatus()); //Unauthorized
  }

  @Test
  public void testSetAndGetUserBean()
  {
    final String token = registerUserForAuthToken(ROLE2);
    final UserForTest user = new UserForTest("test", "user", ROLE1);
    target("/someSecuredService/setUser") //
        .request() //
        .header("Authorization", "Bearer " + token) //
        .put(Entity.entity(user, MediaType.APPLICATION_JSON_TYPE));

    final UserForTest resultUser = target("/someSecuredService/getUser") //
        .request() //
        .header("Authorization", "Bearer " + token) //
        .get(UserForTest.class);

    assertNotNull(resultUser);
    assertEquals(user.getValue(UserForTest.MAIL), resultUser.getValue(UserForTest.MAIL));
    assertEquals(user.getValue(UserForTest.DISPLAY_NAME), resultUser.getValue(UserForTest.DISPLAY_NAME));
    assertEquals(user.getValue(UserForTest.USER_ROLE), resultUser.getValue(UserForTest.USER_ROLE));
  }

  @Test
  public void testBadRequest()
  {
    final Response response = target("/someSecuredService/badRequest").request().get();
    assertEquals(400, response.getStatus());
  }

  private String registerUserForAuthToken(EUserRoleForTest pUserRole)
  {
    final String userMail = UUID.randomUUID().toString() + "@test.de";
    final RegistrationRequestForTest registrationRequest = new RegistrationRequestForTest(userMail, USER_DISPLAY_NAME, pUserRole);
    return target("/authentication/register").request() //
        .post(Entity.entity(registrationRequest, MediaType.APPLICATION_JSON_TYPE), AuthenticationResponse.class) //
        .getValue(TOKEN);
  }

  private static class TestContainerProxy implements TestContainer
  {
    private final TestContainer delegate;

    TestContainerProxy(TestContainer pDelegate)
    {
      delegate = pDelegate;
    }

    @Override
    public ClientConfig getClientConfig()
    {
      return delegate.getClientConfig();
    }

    @Override
    public URI getBaseUri()
    {
      return delegate.getBaseUri();
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }
  }
}
