package de.adito.ojcms.beans.reactive.events;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.reactive.AbstractContainerChangeEvent;

/**
 * A bean has been added to a container.
 *
 * @param <BEAN> the type of the added bean
 * @author Simon Danner, 22.11.2018
 */
public final class BeanContainerAddition<BEAN extends IBean> extends AbstractContainerChangeEvent<BEAN, BeanContainerAddition<BEAN>>
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
