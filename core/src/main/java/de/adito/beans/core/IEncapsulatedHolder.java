package de.adito.beans.core;

/**
 * Beinhaltet die abgekapselten Daten eines Bean-Elementes.
 *
 * @param <ENCAPSULATED> der Typ der Daten-Kerns (siehe IEncapsulated)
 * @author s.danner, 20.01.2017
 */
interface IEncapsulatedHolder<ENCAPSULATED extends IEncapsulated>
{
  /**
   * Liefert den abgekapselten Daten-Kern dieses Elements.
   * Wird als 'virtual-field' für IBean und IBeanContainer verwendet.
   */
  ENCAPSULATED getEncapsulated();
}
