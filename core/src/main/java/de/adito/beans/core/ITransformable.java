package de.adito.beans.core;

import de.adito.beans.core.util.exceptions.AlreadyTransformedException;
import de.adito.beans.core.util.exceptions.NotTransformedException;

import java.util.function.Supplier;

/**
 * Definiert grundlegend eine grafische Komponente, welche in der Lage ist eine Bean-Komponente abzubilden.
 * Unter Transformation wird hierbei, grob gesagt, das Hinzufügen einer Bean-Hülle (siehe IBean/IBeanContainer) zu einer grafischen Komponente verstanden.
 * Es wird die logische Bean-Definition als grafische Komponente abgebildet.
 *
 * Dabei muss festgelegt werden, welches logische Element (IField, IBean oder IBeanContainer) zu welcher grafischen Komponente transformiert werden soll.
 *
 * Es ergeben sich diese (sinnvollen) Kombinationen:
 *
 * - IField -> Unterkomponente in einer Parent-Grafik-Komponente (z.B. Login-Formular)
 * - IBean -> Grafische Komponente, welche den Bean direkt darstellt
 * - IBean (in einem Container) -> Unterkomponente in einem Parent-Grafik-Container
 * - IBeanContainer -> Grafische Komponente, welche den Bean-Container direkt darstellt
 *
 * @param <LOGIC>        der logische Bean-Element Typ (IField, IBean oder IBeanContainer), welches transformiert werden soll
 * @param <VISUAL>       der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <ENCAPSULATED> der Typ des Daten-Kerns der zu transformierenden Quelle
 * @param <SOURCE>       der Typ der Quelle, die transformiert werden soll
 * @author s.danner, 27.01.2017
 */
interface ITransformable<LOGIC, VISUAL, ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>>
    extends IEncapsulatedHolder<ENCAPSULATED>
{
  /**
   * Liefert den Transformator, welcher die hier beschriebene Transformation durchführt.
   * Wenn dieser noch nicht erzeugt ist, geschieht dies vorher.
   */
  IVisualTransformator<LOGIC, VISUAL, ENCAPSULATED, SOURCE> getTransformator();

  /**
   * Erzeugt den Transformator und führt die Transformation durch.
   *
   * @param pSourceToTransform die Quelle, die transformiert werden soll
   */
  default void transform(SOURCE pSourceToTransform)
  {
    assert getTransformator() != null;
    getTransformator().initTransformation(pSourceToTransform);
    assert getOriginalSource() != null; //Transformation muss an dieser Stelle vollzogen sein
    getOriginalSource().getEncapsulated().registerWeakLink(this);
    //Nun können Operationen ausgeführt werden, die in der Warteschlange auf eine vollzogene Transformation warten
    try
    {
      synchronized (getTransformator().getBeforeTransformationQueue())
      {
        getTransformator().getBeforeTransformationQueue().forEach(Runnable::run);
      }
    }
    catch (UnsupportedOperationException pE)
    {
      //Ignorieren, dann muss auch nichts im Nachhinein ausgeführt werden
    }
  }

  /**
   * Gibt an, ob die Komponente bereits transformiert wurde
   *
   * @return <tt>true</tt>, wenn die Komponente bereits transformiert wurde
   */
  default boolean isTransformed()
  {
    return getOriginalSource() != null && getOriginalSource().getEncapsulated().isLinked(this);
  }

  /**
   * Reiht eine Aktion, welche eine abgeschlossene Transformation voraussetzt, in eine Warteschlange ein.
   * Die komplette Warteschlange wird automatisch abgearbeitet, sobald die Transformation durchgeführt wurde.
   *
   * @param pOperation die Operation, welche in die Warteschlange versetzt werden soll
   */
  default void queueOperation(Runnable pOperation) throws UnsupportedOperationException
  {
    if (isTransformed())
      throw new AlreadyTransformedException(getClass().getSimpleName());

    assert getTransformator() != null;
    synchronized (getTransformator().getBeforeTransformationQueue())
    {
      getTransformator().getBeforeTransformationQueue().add(pOperation);
    }
  }

  /**
   * Wirft eine AditoNotTransformedException, falls die Komponente noch nicht transformiert wurde
   */
  default void transformedOrThrow() throws NotTransformedException
  {
    if (!isTransformed())
      throw new NotTransformedException(getClass().getSimpleName());
  }

  /**
   * Wirft eine beliebige Exception, falls die Komponente noch nicht transformiert wurde
   *
   * @param pThrowable  Supplier für die Exception
   * @param <THROWABLE> der Typ der Exception
   */
  default <THROWABLE extends Throwable> void transformedOrThrow(Supplier<THROWABLE> pThrowable) throws THROWABLE
  {
    if (!isTransformed())
      throw pThrowable.get();
  }

  /**
   * Liefert die Original-Quelle dieser Transformation.
   * Wichtig: Darf nur verwendet werden, wenn die Transformation schon durchgeführt wurde.
   */
  default SOURCE getOriginalSource()
  {
    assert getTransformator().getOriginalSource() != null;
    return getTransformator().getOriginalSource();
  }

  @Override
  default ENCAPSULATED getEncapsulated()
  {
    return getOriginalSource().getEncapsulated();
  }
}
