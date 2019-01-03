package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;
import java.util.function.BiPredicate;

/**
 * Marks a bean field as an optional field.
 * Optional fields are created trough {@link de.adito.ojcms.beans.OJFields#createOptional(Class, BiPredicate)}.
 * This annotation is only retained in the source code and is not necessary, but should be used for better readability.
 *
 * @author Simon Danner, 17.08.2017
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface OptionalField
{
}
