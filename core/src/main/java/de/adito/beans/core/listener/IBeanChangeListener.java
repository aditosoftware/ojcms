package de.adito.beans.core.listener;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;

/**
 * Definiert einen Listener, welcher informiert, wenn der Wert eines Bean-Feldes in einer Bean verändert wurde.
 *
 * @param <BEAN> der Typ de Bean, zu welchem dieser Listener registriert werden soll
 * @author s.danner, 23.08.2016
 */
public interface IBeanChangeListener<BEAN extends IBean<BEAN>>
{
  /**
   * Der Wert eines Bean-Feldes wurde verändert.
   * Diese Methode definiert, wie darauf reagiert werden soll.
   *
   * @param pBean     die betreffende Bean
   * @param pField    das betroffene Feld
   * @param pOldValue der vorherige Wert des Feldes
   * @param <TYPE>    der Daten-Typ des Feldes, welches verändert wurde
   */
  default <TYPE> void beanChanged(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
  {
  }

  /**
   * Der Bean wurde ein Feld hinzugefügt.
   * Diese Methode definiert, wie darauf reagiert werden soll.
   *
   * @param pBean  die Bean, zu welcher das Feld hinzugefügt wurde
   * @param pField das Feld, welches hinzugefügt wurde
   * @param <TYPE> der generische Datentyp des Bean-Feldes
   */
  default <TYPE> void fieldAdded(BEAN pBean, IField<TYPE> pField)
  {
  }

  /**
   * Der Bean wurde ein Feld entfernt.
   * Diese Methode definiert, wie darauf reagiert werden soll.
   *
   * @param pBean     die Bean, von welcher das Feld entfernt wurde
   * @param pField    das Feld, welches entfernt wurde
   * @param pOldValue der Wert, welcher vor der Entfernung für dieses Feld gesetzt war
   * @param <TYPE>    der generische Datentyp des Bean-Feldes
   */
  default <TYPE> void fieldRemoved(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
  {
  }
}
