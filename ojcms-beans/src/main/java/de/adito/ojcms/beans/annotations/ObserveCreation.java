package de.adito.ojcms.beans.annotations;

import de.adito.ojcms.beans.BeanCreationEvents;

import java.lang.annotation.*;

/**
 * Marks a bean to inform about its creation.
 * Use {@link BeanCreationEvents} to observe any creation event.
 *
 * @author Simon Danner, 09.02.2018
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObserveCreation
{
}
