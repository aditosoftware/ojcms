package de.adito.beans.core.reactive.events;

import de.adito.beans.core.*;
import de.adito.beans.core.reactive.AbstractContainerChangeEvent;

/**
 * A bean has been added to a container.
 *
 * @param <BEAN> the type of the added bean
 * @author Simon Danner, 22.11.2018
 */
public final class BeanContainerAddition<BEAN extends IBean<BEAN>> extends AbstractContainerChangeEvent<BEAN, BeanContainerAddition<BEAN>>
{
  /**
   * Creates the addition event.
   *
   * @param pSource the container the bean has been added to
   * @param pBean   the added bean
   */
  public BeanContainerAddition(IBeanContainer<BEAN> pSource, BEAN pBean)
  {
    super(pSource, pBean);
  }
}
