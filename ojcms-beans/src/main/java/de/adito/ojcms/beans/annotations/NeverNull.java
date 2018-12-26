package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Annotates bean field types or actual declared bean fields to never have a 'null' data value.
 * Whenever 'null' should be set as a new value, a runtime exception will be thrown if the annotation is present.
 *
 * @author Simon Danner, 25.12.2018
 */
@Documented
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NeverNull
{
}
