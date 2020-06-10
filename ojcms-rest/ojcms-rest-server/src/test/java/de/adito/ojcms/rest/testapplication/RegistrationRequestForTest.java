package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.beans.OJFields;
import de.adito.ojcms.beans.annotations.FinalNeverNull;
import de.adito.ojcms.beans.literals.fields.types.EnumField;
import de.adito.ojcms.rest.auth.api.RegistrationRequest;

/**
 * The registration request for testing purposes adding an user role.
 *
 * @author Simon Danner, 22.04.2020
 */
public class RegistrationRequestForTest extends RegistrationRequest
{
  @FinalNeverNull
  public static final EnumField<EUserRoleForTest> USER_ROLE = OJFields.create(RegistrationRequestForTest.class);

  public RegistrationRequestForTest(String pUserMail, String pDisplayName, EUserRoleForTest pUserRole)
  {
    super(pUserMail, pDisplayName);
    setValue(USER_ROLE, pUserRole);
  }

  /**
   * Required for serialization.
   */
  @SuppressWarnings("unused")
  private RegistrationRequestForTest()
  {
  }
}
