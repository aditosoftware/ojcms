package de.adito.beans.core;

import de.adito.beans.core.util.BeanReflector;

/**
 * Beschreibt die konkrete Basis-Klasse einer Bean. Hier wird der Datenkern gehalten.
 * Initialisiert den Datenkern mit allen Bean-Feldern und null als zugehörigem Wert pro Feld.
 *
 * Zusätzlich gibt es hier zwei protected-Methoden, welche es erlauben, die Werte von private-Feldern zu erhalten und zu ändern.
 * Dadurch können Getter und Setter in den speziellen Beans definiert werden (wie in einer gewöhnlichen Java-Klasse)
 *
 * @param <BEAN> der eigentliche Typ der Bean
 * @author s.danner, 23.08.2016
 */
public class Bean<BEAN extends IBean<BEAN>> implements IBean<BEAN>
{
  private final IBeanEncapsulated<BEAN> encapsulated;

  public Bean()
  {
    encapsulated = new BeanMapEncapsulated<>(getClass(), BeanReflector.getBeanMetadata(getClass()));
  }

  @Override
  public IBeanEncapsulated<BEAN> getEncapsulated()
  {
    return encapsulated;
  }

  /**
   * Liefert den Wert eines private-Feldes.
   *
   * @param pField das Feld, wovon der Wert geliefert werden soll
   * @param <TYPE> der Daten-Typ des Feldes
   * @return der Wert des Feldes
   */
  protected <TYPE> TYPE getPrivateValue(IField<TYPE> pField)
  {
    return encapsulated.getValue(pField);
  }

  /**
   * Setzt den Wert eines private-Feldes
   *
   * @param pField das Feld, wovon der Wert gesetzt werden soll
   * @param pValue der neue Wert
   * @param <TYPE> der Datentyp des Wertes
   */
  protected <TYPE> void setPrivateValue(IField<TYPE> pField, TYPE pValue)
  {
    encapsulated.setValue(pField, pValue);
  }
}
