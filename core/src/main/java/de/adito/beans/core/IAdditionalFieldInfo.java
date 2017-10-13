package de.adito.beans.core;

/**
 * Beschreibt den Identifier einer beliebigen zusätzlichen Information eines Bean-Feldes.
 *
 * @param <TYPE> Beschreibt den Datentyp der Information
 * @author s.danner, 01.06.2017
 * @see IField
 */
public interface IAdditionalFieldInfo<TYPE>
{
  /**
   * Liefert den Typen des Information, welche von dieser Beschreibung identifiziert wird
   */
  Class<TYPE> getType();
}
