package de.adito.beans.core.annotations.internal;

import java.lang.annotation.*;

/**
 * Annotates methods that manipulate some object's data as a write operation.
 *
 * @author Simon Danner, 08.12.2018
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteOperation
{
}