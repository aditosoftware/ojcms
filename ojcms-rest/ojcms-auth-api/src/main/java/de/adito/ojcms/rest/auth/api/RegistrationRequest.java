package de.adito.ojcms.rest.auth.api;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.literals.fields.types.TextField;

/**
 * Defines an user registration request.
 *
 * @author Simon Danner, 07.12.2019
 */
public class RegistrationRequest extends OJBean
{
  @FinalNeverNull
  @FieldOrder(0)
  public static final TextField USER_MAIL = OJFields.create(RegistrationRequest.class);

  @FinalNeverNull
  @FieldOrder(1)
  public static final TextField DISPLAY_NAME = OJFields.create(RegistrationRequest.class);

  public RegistrationRequest(String pUserMail, String pDisplayName)
  {
    setValue(USER_MAIL, pUserMail);
    setValue(DISPLAY_NAME, pDisplayName);
  }

  /**
   * Required for serialization.
   */
  @SuppressWarnings("unused")
  protected RegistrationRequest()
  {
  }
}
