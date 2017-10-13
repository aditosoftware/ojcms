package de.adito.beans.core.annotations;

import de.adito.picoservice.PicoService;

import java.lang.annotation.*;

/**
 * Annotation für ein Bean-Feld, welches das Standardfeld für einen bestimmten Datentypen ist.
 * Über den PicoService wird zur Laufzeit ein Mapping von Datentyp nach Standard-Feld-Typ hergestellt.
 *
 * @author s.danner, 29.06.2017
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PicoService
public @interface TypeDefaultField
{
  /**
   * Liefert die Datentypen, für welche dieses Feldes der Standard-Typ ist.
   * Dabei muss es sich zwingend um den Grundtypen und optional um die Quell-Typen von registrierten Konvertern handeln.
   */
  Class[] types();
}
