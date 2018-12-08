package de.adito.beans.core.annotations.internal;

import java.lang.annotation.*;

/**
 * Marker interface for classes that require access to the encapsulated data core of a bean element.
 * This is just for internal information and will not be retained until runtime.
 *
 * @author Simon Danner, 08.12.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RequiresEncapsulatedAccess
{
}
