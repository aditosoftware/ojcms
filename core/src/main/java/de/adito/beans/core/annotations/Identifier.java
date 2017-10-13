package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Markiert ein Feld einer Bean als Identifikation für das Auffinden in einem Bean-Container (vgl. ID-Spalte in DB)
 * Eine Bean kann mehrere ID-Felder besitzen.
 *
 * @author s.danner, 11.11.2016
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Identifier
{
}
