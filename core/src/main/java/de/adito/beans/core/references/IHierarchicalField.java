package de.adito.beans.core.references;

import de.adito.beans.core.IField;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Beschreibt ein Bean-Feld, welches eine hierarchische Referenz zu anderen Beans herstellt.
 *
 * @param <TYPE> der Datentyp des Feldes
 * @author s.danner, 29.08.2017
 */
public interface IHierarchicalField<TYPE> extends IField<TYPE>
{
  /**
   * Liefert anhand des Wertes des Feldes alle Elemente, welche dadurch referenziert werden.
   *
   * @param pValue der Datenwert dieses Feldes
   * @return eine Menge von IReferables
   */
  Collection<IReferable> getReferables(@Nullable TYPE pValue);
}
