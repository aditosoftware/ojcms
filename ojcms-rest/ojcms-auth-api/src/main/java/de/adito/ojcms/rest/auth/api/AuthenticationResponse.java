package de.adito.ojcms.rest.auth.api;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.literals.fields.types.TextField;

/**
 * Base class for the response of an authentication.
 * Contains the JWT for the HTTP requests and the password for the next authentication.
 * An user of the framework might extend this class to add more information.
 *
 * @author Simon Danner, 25.10.2019
 */
public class AuthenticationResponse extends OJBean
{
  @FinalNeverNull
  @FieldOrder(0)
  public static final TextField TOKEN = OJFields.create(AuthenticationResponse.class);

  @FinalNeverNull
  @FieldOrder(1)
  public static final TextField NEXT_PASSWORD = OJFields.create(AuthenticationResponse.class);

  public AuthenticationResponse(String pToken, String pNextPassword)
  {
    setValue(TOKEN, pToken);
    setValue(NEXT_PASSWORD, pNextPassword);
  }

  /**
   * Required for serialization.
   */
  @SuppressWarnings("unused")
  protected AuthenticationResponse()
  {
  }
}
