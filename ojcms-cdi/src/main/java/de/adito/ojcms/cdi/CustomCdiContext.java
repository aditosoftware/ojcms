package de.adito.ojcms.cdi;

import de.adito.picoservice.PicoService;

import java.lang.annotation.*;

/**
 * Marker annotation for {@link AbstractCustomContext} types to be detected by {@link PicoService}.
 * Through this detection process custom scopes can be registered with the CDI container centrally.
 *
 * @author Simon Danner, 29.12.2019
 */
@Documented
@PicoService
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomCdiContext
{
}
