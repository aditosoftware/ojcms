package de.adito.ojcms.rest.auth.util;

import java.time.Duration;
import java.util.regex.*;

/**
 * Shared utilities between server and client.
 *
 * @author Simon Danner, 30.11.2019
 */
public final class SharedUtils
{
  public static final int CODE_LENGTH = 8;
  public static final Duration RESTORE_CODE_EXPIRATION_THRESHOLD = Duration.ofMinutes(10);

  public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

  private SharedUtils()
  {
  }

  /**
   * Validates a string by a given regex pattern. There are patterns provided by this class statically.
   *
   * @param pPattern  the pattern to verify against
   * @param pToVerify the string to verify
   * @return <tt>true</tt> if the string applies to the given pattern correctly
   */
  public static boolean validatePattern(Pattern pPattern, String pToVerify)
  {
    if (pToVerify == null || pToVerify.isEmpty())
      return false;

    final Matcher matcher = pPattern.matcher(pToVerify);
    return matcher.find();
  }
}
