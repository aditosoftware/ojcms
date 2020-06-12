package de.adito.ojcms.rest.security.user.exceptions;

import de.adito.ojcms.rest.security.user.OJUser;

/**
 * Indicates that an authentication restore code is already active for a certain user.
 *
 * @author Simon Danner, 12.06.2020
 */
public class RestoreCodeAlreadyActive extends Exception
{
  public RestoreCodeAlreadyActive(OJUser pUser)
  {
    super("A restore code is already active for user mail " + pUser
        .getValue(OJUser.MAIL) + "! Cannot create another until the active one expires!");
  }
}
