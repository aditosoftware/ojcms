package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Marks a bean field as identification element within a container component.
 * A bean may have more than one ID field.
 * This information can be compared to a primary-key in a relational database system.
 *
 * @author Simon Danner, 11.11.2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Identifier
{
}
