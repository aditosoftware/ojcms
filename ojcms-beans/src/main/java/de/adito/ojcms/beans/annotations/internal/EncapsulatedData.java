package de.adito.ojcms.beans.annotations.internal;

import java.lang.annotation.*;

/**
 * Marker interface for classes that encapsulate data that should not be visible to API users.
 * This is just for internal information and will not be retained until runtime.
 *
 * @author Simon Danner, 08.12.2018
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EncapsulatedData
{
}
