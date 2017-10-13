package de.adito.beans.core.annotations;

import java.lang.annotation.*;

/**
 * Markiert ein Feld einer Beans oder eine Bean (Container-Element-Anzahl) als Statistics-Provider.
 * Dabei muss ein Intervall und eine maximale Kapazität für die Statistiken definiert werden.
 * Die Statistiken können über Bean bzw. Bean-Container abgefragt werden.
 *
 * @author s.danner, 14.02.2017
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Statistics
{
  /**
   * Liefert das Intervall in Sekunden, in dem ein statistischer Eintrag angefordert werden soll.
   */
  int intervall();

  /**
   * Liefert die maximale Kapazität in Sekunden (Zeitspanne) für diese Statistik.
   */
  int capacity();
}
