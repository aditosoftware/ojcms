package de.adito.ojcms.rest.auth.api;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.literals.fields.types.TextField;

/**
 * Defines an authentication request for the authentication REST resource.
 *
 * @author Simon Danner, 07.12.2019
 */
public class AuthenticationRequest extends OJBean
{
  @FinalNeverNull
  @FieldOrder(0)
  public static final TextField USER_MAIL = OJFields.create(AuthenticationRequest.class);

  @FinalNeverNull
  @FieldOrder(1)
  public static final TextField PASSWORD = OJFields.create(AuthenticationRequest.class);

  public AuthenticationRequest(String pUserMail, String pPassword)
  {
    setValue(USER_MAIL, pUserMail);
    setValue(PASSWORD, pPassword);
  }

  /**
   * Required for serialization.
   */
  @SuppressWarnings("unused")
  private AuthenticationRequest()
  {
  }
}
