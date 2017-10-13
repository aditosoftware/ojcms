package de.adito.beans.core;

import java.util.*;

/**
 * Grundlegende Implementierung eines Bean-Containers. Beinhaltet den Daten-Kern.
 * Ein Bean-Container ist dann gleich einem anderen, wenn alle Beans darin gleich sind.
 *
 * @param <BEAN> der Typ der Beans, welche in dem Container enthalten sind
 * @author s.danner, 23.08.2016
 */
public class BeanContainer<BEAN extends IBean<BEAN>> implements IBeanContainer<BEAN>
{
  private final IBeanContainerEncapsulated<BEAN> encapsulated;

  /**
   * Erzeugt einen neuen Bean-Container.
   *
   * @param pBeanType der Typ der Beans, welche im Container enthalten sind
   */
  public BeanContainer(Class<BEAN> pBeanType)
  {
    this(pBeanType, Collections.emptyList());
  }

  /**
   * Erzeugt einen neuen Bean-Container.
   *
   * @param pBeanType der Typ der Beans, welche im Container enthalten sind
   * @param pBeans    die initiale Menge von Beans des Containers
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
    return "BeanContainer{beanType: " + getBeanType().getSimpleName() + ", count: " + size() + "}";
  }
}
