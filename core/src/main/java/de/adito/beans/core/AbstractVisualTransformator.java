package de.adito.beans.core;

import java.util.*;

/**
 * Abstrakte Grundlage für einen Visual-Transformator.
 * Dieser übernimmt das Speichern der Original-Quelle.
 *
 * @param <LOGIC>        der logische Bean-Element Typ (IField, IBean oder IBeanContainer), welcher transformiert werden soll
 * @param <VISUAL>       der Typ der grafischen Komponente, zu welcher das logische Element transformiert werden soll
 * @param <ENCAPSULATED> der Typ des Daten-Kerns der zu transformierenden Quelle
 * @param <SOURCE>       der Typ der Quelle, die transformiert werden soll
 * @author s.danner, 07.02.2017
 */
abstract class AbstractVisualTransformator<LOGIC, VISUAL, ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>>
    implements IVisualTransformator<LOGIC, VISUAL, ENCAPSULATED, SOURCE>
{
  private SOURCE originalSource;
  private final Queue<Runnable> beforeTransformationQueue = new LinkedList<>();

  @Override
  public void initTransformation(SOURCE pSourceToTransform)
  {
    originalSource = pSourceToTransform;
  }

  @Override
  public SOURCE getOriginalSource()
  {
    return originalSource;
  }

  @Override
  public Queue<Runnable> getBeforeTransformationQueue()
  {
    return beforeTransformationQueue;
  }
}
