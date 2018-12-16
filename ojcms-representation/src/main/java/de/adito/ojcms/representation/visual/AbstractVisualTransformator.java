package de.adito.ojcms.representation.visual;

import java.util.*;

/**
 * Abstract base class for a visual transformator.
 * Stores the reference to the original source and provides a queue to store operations to perform after the transformation.
 *
 * @param <LOGIC>        the logic bean type to transform
 * @param <VISUAL>       the visual counter type to which the logic part will be transformed
 * @param <SOURCE>       the type of the source to transform
 * @author Simon Danner, 07.02.2017
 */
abstract class AbstractVisualTransformator<LOGIC, VISUAL, SOURCE extends ILinkable> implements IVisualTransformator<LOGIC, VISUAL, SOURCE>
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
