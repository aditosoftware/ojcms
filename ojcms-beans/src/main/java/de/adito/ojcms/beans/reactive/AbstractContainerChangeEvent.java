package de.adito.ojcms.beans.reactive;

import de.adito.ojcms.beans.*;

/**
 * An abstract bean container change event.
 *
 * @param <BEAN>  the type of beans contained in the source container
 * @param <EVENT> the actual runtime type of this event
 * @author Simon Danner, 18.11.2018
 */
public abstract class AbstractContainerChangeEvent<BEAN extends IBean, EVENT extends AbstractContainerChangeEvent<BEAN, EVENT>>
    extends AbstractChangeEvent<IBeanContainer<BEAN>, EVENT>
{
  private final BEAN bean;

  /**
   * Creates the container based event.
   *
   * @param pSource the bean container that is the source which triggered this event
   * @param pBean   the bean affected by the change (addition or removal in general)
   */
  protected AbstractContainerChangeEvent(IBeanContainer<BEAN> pSource, BEAN pBean)
  {
    super(pSource);
    bean = pBean;
  }

  /**
   * The bean affected by the change event.
   *
   * @return the affected bean
   */
  public BEAN getBean()
  {
    return bean;
  }
}
