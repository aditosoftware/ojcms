package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Final modifier annotation for bean fields. Use this annotation to define a bean field for which the value can only be set once.
 *
 * @author Simon Danner, 13.10.2019
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Final
{
}
