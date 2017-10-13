package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Markiert ein Feld einer Bean als Detail.
 * Wie diese Information angewendet wird, muss von der jeweiligen Applikation entschieden werden.
 *
 * @author s.danner, 23.08.2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Detail
{
}
