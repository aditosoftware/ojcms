package de.adito.beans.core;

import de.adito.beans.core.util.exceptions.*;

import java.util.function.Supplier;

/**
 * Basic interface for a graphical representation of a bean element.
 * A visual component will be transformed to the bean element. The original bean or bean container still exists afterwards.
 * The graphical component can be treated as the original bean element.
 * To achieve this, the bean interfaces, which are functional wrappers {@link IBean} {@link IBeanContainer},
 * will be extended to the transformable component.
 * Through providing a reference to the bean core {@link IEncapsulated} the transformation can be executed.
 * For detailed information of the separation of functional wrapper and bean cores, take a look at the interfaces above.
 *
 * A transformation can be performed on different levels. (That means, wich bean element will be transformed to what graphical part)
 * These are these reasonable possibilities:
 *
 * - {@link IField} -> sub component of a graphical parent component. (e.g. login form)
 * - {@link IBean} -> graphical component that represents the bean directly
 * - {@link IBean} (within a container) -> sub component for one bean within a graphical container component (e.g. table)
 * - {@link IBeanContainer} -> graphical component that represents the bean directly
 *
 * The transformation level will be defined trough the first two generic types.
 * The transformator, which will be used for the transformation {@link IVisualTransformator}, must be based on the same generic types.
 *
 * @param <LOGIC>        the logical level of the transformation (field, bean or container)
 * @param <VISUAL>       the type of the graphical components to which the logical components will be transformed to
 * @param <ENCAPSULATED> the type of the data core of the transformation source
 * @param <SOURCE>       the type of the source (bean element) that will be used for the transformation
 * @author Simon Danner, 27.01.2017
 */
interface ITransformable<LOGIC, VISUAL, ENCAPSULATED extends IEncapsulated, SOURCE extends IEncapsulatedHolder<ENCAPSULATED>>
    extends IEncapsulatedHolder<ENCAPSULATED>
{
  /**
   * The transformator, that performs the transformation of the single components.
   * Must be based on the same generic types, which define the transformation level.
   *
   * @return a visual transformator
   */
  IVisualTransformator<LOGIC, VISUAL, ENCAPSULATED, SOURCE> getTransformator();

  /**
   * Performs the transformation.
   * Stores the data core's reference at the transformator, which will be used as data holder.
   * Furthermore a link will be registered at the original bean element.
   * The before-transformation-queue will be executed finally.
   *
   * @param pSourceToTransform the source bean element of this transformation
   */
  default void transform(SOURCE pSourceToTransform)
  {
    assert getTransformator() != null;
    getTransformator().initTransformation(pSourceToTransform);
    assert getOriginalSource() != null; //the transformation must be completed now
    getOriginalSource().getEncapsulated().registerWeakLink(this);
    //Perform all operations that were waiting for the completion of the transformation
    try
    {
      synchronized (getTransformator().getBeforeTransformationQueue())
      {
        getTransformator().getBeforeTransformationQueue().forEach(Runnable::run);
      }
    }
    catch (UnsupportedOperationException pE)
    {
      //Can be ignored (there is no queue then)
    }
  }

  /**
   * Determines, if this component has already been transformed.
   *
   * @return <tt>true</tt>, if the component is transformed
   */
  default boolean isTransformed()
  {
    try
    {
      return getEncapsulated().isLinked(this);
    }
    catch (NotTransformedException pE)
    {
      return false;
    }
  }

  /**
   * Queues a single operation that requires a completed transformation.
   * The queue will be executed right after the transformation.
   *
   * @param pOperation the operation to queue
   * @throws UnsupportedOperationException may throw an exception, if there is no operation container provided by the transformator
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
   * Throws a {@link NotTransformedException} if the transformation is not completed.
   * May be used as an 'assertion' within the development.
   */
  default void transformedOrThrow() throws NotTransformedException
  {
    transformedOrThrow(() -> new NotTransformedException(getClass().getName()));
  }

  /**
   * Throws a definable exception, if the transformation is not completed.
   *
   * @param pThrowable  supplier for the exception
   * @param <THROWABLE> the exception's type
   */
  default <THROWABLE extends Throwable> void transformedOrThrow(Supplier<THROWABLE> pThrowable) throws THROWABLE
  {
    if (!isTransformed())
      throw pThrowable.get();
  }

  /**
   * The original bean element. (source of the transformation)
   * Will be used to provide the data core's reference for the functional wrapper interfaces. (see {@link IBean}
   * Important: can only be used if the transformation has been completed
   */
  default SOURCE getOriginalSource()
  {
    if (getTransformator() == null || getTransformator().getOriginalSource() == null)
      throw new NotTransformedException(getClass().getName());
    return getTransformator().getOriginalSource();
  }

  @Override
  default ENCAPSULATED getEncapsulated()
  {
    return getOriginalSource().getEncapsulated();
  }
}
