package de.adito.beans.core;

import java.util.*;

/**
 * A default implementation of the bean container interface.
 * It stores the encapsulated data core.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 23.08.2016
 */
public class BeanContainer<BEAN extends IBean<BEAN>> implements IBeanContainer<BEAN>
{
  private final IBeanContainerEncapsulated<BEAN> encapsulated;

  /**
   * Creates a new bean container.
   *
   * @param pBeanType the type of the beans in the container
   */
  public BeanContainer(Class<BEAN> pBeanType)
  {
    this(pBeanType, Collections.emptyList());
  }

  /**
   * Creates a new bean container with a collection of initial beans.
   *
   * @param pBeanType the type of the beans in the container
   * @param pBeans    the initial collection of beans in this container
   */
  public BeanContainer(Class<BEAN> pBeanType, Collection<BEAN> pBeans)
  {
    encapsulated = new BeanListEncapsulated<>(pBeanType, pBeans);
    pBeans.forEach(pBean -> BeanListenerUtil.beanAdded(this, pBean));
  }

  @Override
  public IBeanContainerEncapsulated<BEAN> getEncapsulated()
  {
    return encapsulated;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{beanType: " + getBeanType().getSimpleName() + ", count: " + size() + "}";
  }
}
