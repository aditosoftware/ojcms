package de.adito.beans.core.util;

import de.adito.beans.core.*;

import java.util.AbstractList;

/**
 * A list proxy for a bean container to treat it as a {@link java.util.List}.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 08.02.2017
 */
public class BeanContainerListProxy<BEAN extends IBean<BEAN>> extends AbstractList<BEAN>
{
  private final IBeanContainer<BEAN> container;

  /**
   * Creates the list proxy based on an original bean container.
   *
   * @param pContainer the original bean container
   */
  public BeanContainerListProxy(IBeanContainer<BEAN> pContainer)
  {
    container = pContainer;
  }

  @Override
  public BEAN get(int pIndex)
  {
    return container.getBean(pIndex);
  }

  @Override
  public int size()
  {
    return container.size();
  }

  @Override
  public BEAN set(int pIndex, BEAN pBean)
  {
    return container.replaceBean(pBean, pIndex);
  }

  @Override
  public void add(int pIndex, BEAN pBean)
  {
    container.addBean(pBean, pIndex);
  }

  @Override
  public BEAN remove(int pIndex)
  {
    return container.removeBean(pIndex);
  }
}
