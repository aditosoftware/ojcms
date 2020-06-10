package de.adito.ojcms.rest.auth.api;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.FinalNeverNull;
import de.adito.ojcms.beans.literals.fields.types.TextField;

/**
 * Defines a request to restore authentication for an user with a code.
 *
 * @author Simon Danner, 07.12.2019
 */
public final class RestoreAuthenticationRequest extends OJBean
{
  @FinalNeverNull
  public static TextField USER_MAIL = OJFields.create(RestoreAuthenticationRequest.class);
  @FinalNeverNull
  public static TextField RESTORE_CODE = OJFields.create(RestoreAuthenticationRequest.class);

  /**
   * Creates a new restore request.
   *
   * @param pUserMail    the mail address of the user to restore authentication for
   * @param pRestoreCode the string based restore code
   */
  public RestoreAuthenticationRequest(String pUserMail, String pRestoreCode)
  {
    setValue(USER_MAIL, pUserMail);
    setValue(RESTORE_CODE, pRestoreCode);
  }

  /**
   * Required for serialization.
   */
  @SuppressWarnings("unused")
  private RestoreAuthenticationRequest()
  {
  }
}
