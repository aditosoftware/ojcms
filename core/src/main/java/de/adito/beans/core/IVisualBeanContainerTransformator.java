package de.adito.beans.core;

import java.util.*;
import java.util.function.Consumer;

/**
 * A transformator for bean containers. (Defines the generic types)
 * For more information take a look at the base interfaces.
 *
 * @param <LOGIC>  the logical level of the transformation (bean or container)
 * @param <VISUAL> the type of the graphical components to which the logical components will be transformed to
 * @param <BEAN>   the type of the source beans in the container
 * @author Simon Danner, 13.09.2017
 * @see ITransformable
 */
public interface IVisualBeanContainerTransformator<LOGIC, VISUAL, BEAN extends IBean<BEAN>>
    extends IVisualTransformator<LOGIC, VISUAL, IBeanContainerEncapsulated<BEAN>, IBeanContainer<BEAN>>
{
  /**
   * A container for listeners that will be informed, when the visibility states of the beans within the container change.
   * It is not supported per default, because not every component has to have visibility states.
   *
   * @return the listener for the visibility states
   * @throws UnsupportedOperationException if the container is not available
   */
  default Set<Consumer<Collection<BEAN>>> getWeakVisibleListenerContainer() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Visibility change listeners are not supported. Provide a container!");
  }
}
