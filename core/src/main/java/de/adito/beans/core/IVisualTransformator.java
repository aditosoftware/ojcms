package de.adito.beans.core;

import java.util.Queue;

/**
 * Beschreibt einen Transformator, welcher eine Bean-Transformation (siehe ITransformable) durchführt.
 * Der Transformator dient dabei als Container für den Daten-Kern des Bean-Elementes.
 *
 * @param <LOGIC>        der logische Bean-Element Typ (IField, IBean oder IBeanContainer), welches transformiert werden soll
 * @param <VISUAL>       der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <ENCAPSULATED> der Typ des Daten-Kerns der zu transformierenden Quelle
 * @param <SOURCE>       der Typ der Quelle, die transformiert werden soll
 * @author s.danner, 27.01.2017
 * @see ITransformable
 */
interface IVisualTransformator<LOGIC, VISUAL, ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>>
{
  /**
   * Transformiert den Daten-Kern und initialisiert die Transformation.
   *
   * @param pSourceToTransform die Quelle, die transformiert werden soll
   */
  void initTransformation(SOURCE pSourceToTransform);

  /**
   * Liefert die Original-Quelle der Transformation.
   */
  SOURCE getOriginalSource();

  /**
   * Liefert die grafische Komponente zu einer logischen Bean-Komponente.
   *
   * @param pLogicComponent die logische Bean-Komponente
   * @return die grafische Komponente, welche die logische Komponente abbildet
   */
  VISUAL getVisualComponent(LOGIC pLogicComponent);

  /**
   * Verbindet die logische und die grafische Komponente auf Daten-Basis.
   * Beispiel: Logik: Textfield (in Bean) Visual: Textfeld -> Wird ein Wert in das Textfeld getippt, ist der Wert automatisch im Bean gesetzt.
   *
   * @param pLogicComponent  die logische Bean-Komponente
   * @param pVisualComponent die grafische Komponente, welche die logische Komponente abbildet
   */
  void link(LOGIC pLogicComponent, VISUAL pVisualComponent);

  /**
   * Liefert eine verbundene grafische Komponente zu einer Logik-Komponente.
   * Kombiniert getVisualComponent()  und link().
   *
   * @param pLogicComponent die logische Bean-Komponente
   * @return die grafische Komponente, welche die logische Komponente abbildet
   */
  default VISUAL getLinkedVisualComponent(LOGIC pLogicComponent)
  {
    VISUAL visualComponent = getVisualComponent(pLogicComponent);
    link(pLogicComponent, visualComponent);
    return visualComponent;
  }

  /**
   * Liefert eine Warteschlange, in welcher Operationen vermerkt werden, welche nach der Transformation ausgeführt werden.
   *
   * @return eine Warteschlange von Runnables
   * @throws UnsupportedOperationException muss beim konkreten Transformator umgesetzt werden, falls benötigt
   */
  default Queue<Runnable> getBeforeTransformationQueue() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }
}
