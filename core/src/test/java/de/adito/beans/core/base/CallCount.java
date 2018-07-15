package de.adito.beans.core.base;

import java.lang.annotation.*;

/**
 * Used to annotate tests, that expect a certain amount of calls of a method.
 * This could be very useful, where a method call leads to an indirect call of something to test (e.g. listeners).
 *
 * @author Simon Danner, 15.07.2018
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CallCount
{
  /**
   * The expected call count.
   * Default: 1.
   *
   * @return the call count
   */
  int expectedCallCount() default 1;
}
