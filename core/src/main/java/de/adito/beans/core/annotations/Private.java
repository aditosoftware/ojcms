package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Markiert ein Bean-Feld als private -> Datenkapselung
 *
 * @author s.danner, 31.01.2017
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Private
{
}
