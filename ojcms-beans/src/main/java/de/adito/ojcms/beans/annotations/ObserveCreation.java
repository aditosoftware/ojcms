package de.adito.ojcms.beans.annotations;

import java.lang.annotation.*;

/**
 * Marks a bean to inform about its creation.
 * Use {@link de.adito.ojcms.beans.BeanCreationRegistry} to observe any creation event.
 *
 * @author Simon Danner, 09.02.2018
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObserveCreation
{
}
