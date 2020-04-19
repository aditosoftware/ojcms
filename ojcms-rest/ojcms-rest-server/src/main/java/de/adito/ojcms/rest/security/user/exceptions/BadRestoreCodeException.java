package de.adito.ojcms.rest.security.user.exceptions;

import java.time.Duration;

/**
 * Indicates failures within the user restore management.
 *
 * @author Simon Danner, 23.11.2019
 */
public class BadRestoreCodeException extends Exception
{
  /**
   * Initializes the exception due to an invalid restore code.
   *
   * @param pRestoreCode the invalid restore code
   */
  public BadRestoreCodeException(String pRestoreCode)
  {
    super("Bad restore code: " + pRestoreCode);
  }

  /**
   * Initializes the exception due to an expired restore code.
   *
   * @param pDuration the expiration duration of the code
   */
  public BadRestoreCodeException(Duration pDuration)
  {
    super("Restore code expired after " + pDuration.toMinutes() + " minutes!");
  }
}
