package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Marks a bean field as detail.
 * The application determines how to use this information.
 *
 * @author Simon Danner, 23.08.2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Detail
{
}
