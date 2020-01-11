package de.adito.ojcms.sqlbuilder.executors;

/**
 * A function that may throw an exception.
 *
 * @param <ARG>       the type of the argument of the function
 * @param <RESULT>    the type of the result of the function
 * @param <EXCEPTION> the type of the exception
 * @author Simon Danner, 05.01.2020
 */
@FunctionalInterface
public interface ThrowingFunction<ARG, RESULT, EXCEPTION extends Exception>
{
  /**
   * Returns a result based on one argument.
   *
   * @param pArgument the argument
   * @return the result of this function
   * @throws EXCEPTION if something went wrong there
   */
  RESULT apply(ARG pArgument) throws EXCEPTION;
}
