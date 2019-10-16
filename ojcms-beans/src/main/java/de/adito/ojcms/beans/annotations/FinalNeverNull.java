package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Combines {@link Final} and {@link NeverNull}.
 *
 * @author Simon Danner, 13.10.2019
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinalNeverNull
{
}
