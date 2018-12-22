package de.adito.ojcms.beans.base;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Helper to check if equals and hashCode of two given references lead to equality or not.
 *
 * @author Simon Danner, 22.12.2018
 */
@FunctionalInterface
public interface IEqualsHashCodeChecker
{
  /**
   * Makes an assertion if the two references are considered equal or not.
   *
   * @param pShouldBeEqual <tt>true</tt> if the assertion is that they are equal
   */
  void makeAssertion(boolean pShouldBeEqual);

  /**
   * Creates an instances of the helper.
   *
   * @param object1 the first reference/object to include in the test
   * @param object2 the second reference/object to include in the test
   * @param <TYPE>  the type of the references to check
   * @return the created helper instance
   */
  static <TYPE> IEqualsHashCodeChecker create(TYPE object1, TYPE object2)
  {
    return pShouldBeEqual ->
    {
      assertEquals(pShouldBeEqual, Objects.equals(object1, object2));
      assertEquals(pShouldBeEqual, Objects.hashCode(object1) == Objects.hashCode(object2));
    };
  }
}
