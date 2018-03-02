package de.adito.beans.core.annotations;

import de.adito.beans.core.BeanCreationRegistry;

import java.lang.annotation.*;

/**
 * Marks a bean to inform about its creation.
 * Use {@link BeanCreationRegistry} to listen to any creation event.
 *
 * @author Simon Danner, 09.02.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObserveCreation
{
}
