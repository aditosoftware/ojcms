package de.adito.ojcms.rest.testapplication;

import java.lang.annotation.*;

/**
 * Boundary annotation for testing purposes.
 *
 * @author Simon Danner, 22.04.2020
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TestBoundary
{
  EUserRoleForTest[] requiresOneOfTheseRoles() default {};
}
