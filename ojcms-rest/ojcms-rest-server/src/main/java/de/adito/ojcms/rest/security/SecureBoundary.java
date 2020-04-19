package de.adito.ojcms.rest.security;

import java.lang.annotation.*;

/**
 * REST method annotation to prevent unauthorized access.
 * Every HTTPS request has to provide a JWT (Json web token) to verify their valid identities.
 *
 * @author Simon Danner, 20.09.2019
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecureBoundary
{
}
