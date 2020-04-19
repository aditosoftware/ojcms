package de.adito.ojcms.rest.security.util;

import java.security.SecureRandom;
import java.util.*;

/**
 * Utility class to generate random strings.
 *
 * @author Simon Danner, 07.12.2019
 */
public final class RandomString
{
  private static final String UPPER_ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWER_ABC = UPPER_ABC.toLowerCase(Locale.ROOT);
  private static final String DIGITS = "0123456789";
  private static final char[] ABC = (UPPER_ABC + LOWER_ABC + DIGITS).toCharArray();
  private static final Random RANDOM = new SecureRandom();

  private RandomString()
  {
  }

  /**
   * Generates a new string with a given length. The string will contain letters (upper and lower case) and numbers.
   *
   * @param pLength the length of the string to generate
   * @return the generated string
   */
  public static String generate(int pLength)
  {
    if (pLength < 1)
      throw new IllegalArgumentException("Bad length: " + pLength);

    final char[] buffer = new char[pLength];

    for (int i = 0; i < buffer.length; ++i)
      buffer[i] = ABC[RANDOM.nextInt(ABC.length)];

    return new String(buffer);
  }
}