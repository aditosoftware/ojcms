package de.adito.beans.core;

/**
 * Definiert eine grafische Bean-Komponente, welche die Quell-Komponente direkt abbildet.
 * Entweder: IBean -> direkte grafische Abbildung oder IBean-Container -> direkte grafische Abbildung.
 *
 * Konkret gesagt ist dabei das grafische transformable-Element gleichzeitig der Transformator.
 * Ein erheblicher Unterschied ist hier, dass es keine Grundlage für einen Transformator gibt und somit die Original-Quelle gespeichert werden muss.
 *
 * @param <ENCAPSULATED> der Typ des Daten-Kerns der zu transformierenden Quelle
 * @param <SOURCE>       der Typ der Quelle, die transformiert werden soll
 * @author s.danner, 27.01.2017
 */
interface ISelfTransformable<ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>,
    TRANSFORMATOR extends ISelfVisualTransformator<ENCAPSULATED, SOURCE, TRANSFORMATOR>>
    extends ITransformable<SOURCE, TRANSFORMATOR, ENCAPSULATED, SOURCE>, ISelfVisualTransformator<ENCAPSULATED, SOURCE, TRANSFORMATOR>
{
  @Override
  SOURCE getOriginalSource();

  @Override
  default TRANSFORMATOR getTransformator()
  {
    //noinspection unchecked
    return (TRANSFORMATOR) this;
  }

  @Override
  default void transform(SOURCE pSourceToTransform)
  {
    //noinspection unchecked
    getTransformator().link(pSourceToTransform, (TRANSFORMATOR) this); //Erst linken, dass Operationen ausgeführt werden können (Kern sowieso vorhanden)
    ITransformable.super.transform(pSourceToTransform);
  }

  @Override
  default void initTransformation(SOURCE pSourceToTransform)
  {
    //Die Source muss hier manuell gespeichert werden
  }
}
