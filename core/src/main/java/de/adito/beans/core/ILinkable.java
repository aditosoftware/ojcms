package de.adito.beans.core;

/**
 * Beschreibt eine Schnittstelle, wo transformierbare Komponenten als Link registriert werden können.
 *
 * @author s.danner, 18.07.2017
 */
interface ILinkable
{
  /**
   * Gibt an, ob eine transformierbare Komponente mit dieser Komponente verlinkt ist.
   *
   * @param pComponent die Komponente, zu welcher überprüft werden soll, ob sie verlinkt ist
   * @param <LINK>     der generische Typ der transformierbaren Komponente
   * @return <tt>true</tt>, wenn eine Verlinkung vorhanden ist
   */
  <LINK extends ITransformable> boolean isLinked(LINK pComponent);

  /**
   * Registriert einen schwachen Link zwischen dieser Komponente und einer transformierbaren anderen Komponente.
   *
   * @param pComponent die Komponente, welcher zur Verlinkung registriert werden soll
   * @param <LINK>der  generische Typ der transformierbaren Komponente
   */
  <LINK extends ITransformable> void registerWeakLink(LINK pComponent);
}
