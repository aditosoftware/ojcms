package de.adito.beans.core.listener;

import de.adito.beans.core.IBean;

/**
 * An extension of {@link IBeanChangeListener} for listeners, which react to changes of a bean container.
 * To support containers the addition and removal of beans are also recognized.
 * Changes to single beans should also be supplied by this enhanced listener.
 * The methods are defined as default methods to enable adapter behaviour.
 *
 * @param <BEAN> the type of the beans in the container, to which this listener will be registered
 * @author Simon Danner, 23.08.2016
 */
public interface IBeanContainerChangeListener<BEAN extends IBean<BEAN>> extends IBeanChangeListener<BEAN>
{
  /**
   * A bean has been added to the container.
   *
   * @param pBean the added bean
   */
  default void beanAdded(BEAN pBean)
  {
  }

  /**
   * A bean has been removed from the container
   *
   * @param pBean the removed bean
   */
  default void beanRemoved(BEAN pBean)
  {
  }
}
