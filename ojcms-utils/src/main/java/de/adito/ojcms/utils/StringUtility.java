package de.adito.ojcms.utils;

/**
 * Utility for String.
 *
 * @author Simon Danner, 25.12.2018
 */
public final class StringUtility
{
  private StringUtility()
  {
  }

  /**
   * Checks that a string is not null or empty.
   * Designed to use for parameter validation.
   *
   * @param pStringToCheck the string to check
   * @param pIdentifier    an identifier for the string to check
   * @return the checked not null, not empty string
   */
  public static String requireNotEmpty(String pStringToCheck, String pIdentifier)
  {
    if (pStringToCheck == null || pStringToCheck.trim().isEmpty())
      throw new IllegalArgumentException("The " + pIdentifier + " must not be empty!");
    
    return pStringToCheck;
  }
}
