package de.adito.ojcms.beans.reactive.events;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.reactive.AbstractContainerChangeEvent;

/**
 * A bean has been removed from a container.
 *
 * @param <BEAN> the type of the removed bean
 * @author Simon Danner, 22.11.2018
 */
public final class BeanContainerRemoval<BEAN extends IBean<BEAN>> extends AbstractContainerChangeEvent<BEAN, BeanContainerRemoval<BEAN>>
{
  /**
   * Creates the removal event.
   *
   * @param pSource the container the bean has been removed from
   * @param pBean   the removed bean
   */
  public BeanContainerRemoval(IBeanContainer<BEAN> pSource, BEAN pBean)
  {
    super(pSource, pBean);
  }
}
