package de.adito.beans.core;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * A graphical representation of a bean container.
 * The graphical component uses the bean container interface and refers to the same data core as the original.
 * For further information look at {@link ITransformable}.
 *
 * @param <BEAN>   the bean's types within the container.
 * @param <LOGIC>  the logical level of the transformation (bean or container)
 * @param <VISUAL> the type of the graphical components to which the logical components will be transformed to
 * @author Simon Danner, 07.02.2017
 */
public interface ITransformableBeanContainer<BEAN extends IBean<BEAN>, LOGIC, VISUAL>
    extends IBeanContainer<BEAN>, ITransformable<LOGIC, VISUAL, IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>>
{
  @Override
  IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN> getTransformator();

  /**
   * A stream of all visible beans of this container.
   *
   * @return a stream of beans
   */
  default Stream<BEAN> streamVisibleBeans()
  {
    return stream(); //Default: all are visible
  }

  /**
   * The count of the visible beans of this container.
   *
   * @return the number of visible beans
   */
  default int getVisibleBeanCount()
  {
    return (int) streamVisibleBeans().count();
  }

  /**
   * Registers a listener that gets informed, if the visibility state of the beans within the container is changed.
   * Per default, the container for the listeners is stored at the transformator.
   * If there is no transformator (self-transforming component), the container has to be stored at the component itself to use this feature.
   *
   * @param pListener the listener / action that will be performed when the visibility changes
   * @throws UnsupportedOperationException if there's no container for the listeners available
   */
  default void listenWeakToVisibilityChange(Consumer<Collection<BEAN>> pListener) throws UnsupportedOperationException
  {
    IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN> transformator = getTransformator();
    if (transformator == null || transformator.getWeakVisibleListenerContainer() == null)
      throw new UnsupportedOperationException("Visibility change listeners are not supported for this container. Provide a container!");
    synchronized (transformator.getWeakVisibleListenerContainer())
    {
      transformator.getWeakVisibleListenerContainer().add(pListener);
    }
  }

  /**
   * Fires a visibility change of the beans in the container.
   *
   * @throws UnsupportedOperationException if there's no container for the listeners available
   */
  default void fireVisibilityChange() throws UnsupportedOperationException
  {
    IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN> transformator = getTransformator();
    if (transformator == null || transformator.getWeakVisibleListenerContainer() == null)
      throw new UnsupportedOperationException("Visibility change listeners are not supported for this container. Provide a container!");
    List<BEAN> visibleBeans = streamVisibleBeans().collect(Collectors.toList());
    synchronized (transformator.getWeakVisibleListenerContainer())
    {
      transformator.getWeakVisibleListenerContainer().forEach(pListener -> pListener.accept(visibleBeans));
    }
  }

  /**
   * Compares the visible beans before and after a certain action.
   * If the there is a difference, the listeners will be informed.
   *
   * @param pAction the action to perform while comparing the visibility states
   */
  default void compareVisibilityAfterActionAndFire(Runnable pAction)
  {
    List<BEAN> beforeVisible = streamVisibleBeans().collect(Collectors.toList());
    pAction.run();
    if (!compareVisibleBeans(beforeVisible))
      fireVisibilityChange();
  }

  /**
   * Determines if a certain collection of beans is the same or different from the current visible beans.
   * The comparison is based on references rather than on a logical equals implementation.
   *
   * @param pToCompare the collection of beans that will be compared to the current visible beans
   * @return <tt>true</tt>, if there is no difference
   */
  default boolean compareVisibleBeans(Collection<BEAN> pToCompare)
  {
    return pToCompare.size() == getVisibleBeanCount() &&
        streamVisibleBeans()
            .noneMatch(pBean -> pToCompare.stream()
                .anyMatch(pBeanToCompare -> pBean != pBeanToCompare)); //references!
  }
}
