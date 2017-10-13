package de.adito.beans.core;

/**
 * Definiert einen Transformator für eine Transformable-Komponente, welche das Bean-Element direkt abbildet.
 * Hier werden überflüssige Methoden vom IVisualTransformator default-mäßig implementiert.
 * Die visuelle Komponente für eine logische Komponente ist dabei immer der Transformator selbst.
 *
 * @author s.danner, 27.01.2017
 * @see ISelfTransformable
 */
interface ISelfVisualTransformator<ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>,
    TRANSFORMATOR extends ISelfVisualTransformator<ENCAPSULATED, SOURCE, TRANSFORMATOR>>
    extends IVisualTransformator<SOURCE, TRANSFORMATOR, ENCAPSULATED, SOURCE>
{
  @Override
  default TRANSFORMATOR getVisualComponent(SOURCE pLogicComponent)
  {
    //noinspection unchecked
    return (TRANSFORMATOR) this;
  }

  @Override
  default TRANSFORMATOR getLinkedVisualComponent(SOURCE pLogicComponent)
  {
    //noinspection unchecked
    return (TRANSFORMATOR) this;
  }
}
