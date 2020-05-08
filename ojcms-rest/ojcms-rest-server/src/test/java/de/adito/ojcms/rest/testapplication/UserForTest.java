package de.adito.ojcms.rest.testapplication;

import de.adito.ojcms.beans.OJFields;
import de.adito.ojcms.beans.literals.fields.types.EnumField;
import de.adito.ojcms.persistence.Persist;
import de.adito.ojcms.rest.security.user.OJUser;

/**
 * An {@link OJUser} for testing purposes. Adds a role as addition to the standard user.
 *
 * @author Simon Danner, 22.04.2020
 */
@Persist(containerId = "TEST_USER_CONTAINER")
public class UserForTest extends OJUser
{
  public static final EnumField<EUserRoleForTest> USER_ROLE = OJFields.create(UserForTest.class);

  public UserForTest(String pMailAddress, String pDisplayName, EUserRoleForTest pUserRole)
  {
    super(pMailAddress, pDisplayName);
    setValue(USER_ROLE, pUserRole);
  }

  private UserForTest()
  {
  }
}
