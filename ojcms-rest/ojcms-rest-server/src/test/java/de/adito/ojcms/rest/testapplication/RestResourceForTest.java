package de.adito.ojcms.rest.testapplication;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

/**
 * Rest resource for testing. Contains some secured methods.
 * Also used to test bean serialization.
 *
 * @author Simon Danner, 22.04.2020
 */
@Path("/someSecuredService")
public class RestResourceForTest
{
  private static UserForTest user;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @TestBoundary(requiresOneOfTheseRoles = EUserRoleForTest.ROLE1)
  @Path("/secret")
  public String getSomeSecretValue()
  {
    return "42";
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TestBoundary(requiresOneOfTheseRoles = EUserRoleForTest.ROLE2)
  @Path("/getUser")
  public Response getUserBean()
  {
    return Response.ok().entity(user).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @TestBoundary(requiresOneOfTheseRoles = EUserRoleForTest.ROLE2)
  @Path("/setUser")
  public Response setUserBean(UserForTest pUser)
  {
    user = pUser;
    return Response.noContent().build();
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/badRequest")
  public Response badRequest()
  {
    return Response.status(Response.Status.BAD_REQUEST).build();
  }
}
