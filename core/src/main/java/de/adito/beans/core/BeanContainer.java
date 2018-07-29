package de.adito.beans.core;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A default implementation of the bean container interface.
 * It stores the encapsulated data core.
 * Instances can be created via static methods in {@link IBeanContainer}.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 23.08.2016
 */
public class BeanContainer<BEAN extends IBean<BEAN>> implements IBeanContainer<BEAN>
{
  private final Class<BEAN> beanType;
  private IBeanContainerEncapsulated<BEAN> encapsulated;

  /**
   * Creates a new empty bean container.
   *
   * @param pBeanType the type of the beans in the container
   */
  protected BeanContainer(Class<BEAN> pBeanType)
  {
    this(pBeanType, Collections.emptyList());
  }

  /**
   * Creates a new bean container with a collection of initial beans.
   *
   * @param pBeanType the type of the beans in the container
   * @param pBeans    the initial collection of beans in this container
   */
  protected BeanContainer(Class<BEAN> pBeanType, Iterable<BEAN> pBeans)
  {
    this(pBeanType, new DefaultEncapsulatedBuilder<>(pBeans));
  }

  /**
   * Creates a new bean container with an encapsulated data core
   * based on a {@link de.adito.beans.core.EncapsulatedBuilder.IContainerEncapsulatedBuilder}
   *
   * @param pBeanType the type of the beans in the container
   * @param pBuilder  the encapsulated builder
   */
  protected BeanContainer(Class<BEAN> pBeanType, EncapsulatedBuilder.IContainerEncapsulatedBuilder<BEAN> pBuilder)
  {
    beanType = pBeanType;
    setEncapsulated(pBuilder);
    encapsulated.stream().forEach(pBean -> BeanListenerUtil.beanAdded(this, pBean));
  }

  /**
   * Sets the encapsulated data core for this container.
   * The data core will be created by {@link EncapsulatedBuilder}
   * based on a {@link de.adito.beans.core.EncapsulatedBuilder.IContainerEncapsulatedBuilder}.
   *
   * @param pBuilder the builder to create the data core
   */
  void setEncapsulated(EncapsulatedBuilder.IContainerEncapsulatedBuilder<BEAN> pBuilder)
  {
    encapsulated = EncapsulatedBuilder.createContainerEncapsulated(pBuilder, beanType);
  }

  @Override
  public IBeanContainerEncapsulated<BEAN> getEncapsulated()
  {
    return encapsulated;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{beanType: " + getBeanType().getSimpleName() + ", count: " + size() + "}\nbeans:\n" +
        stream()
            .map(Objects::toString)
            .collect(Collectors.joining("\n"));
  }

  /**
   * Default encapsulated data core based on a list to store the beans of this container.
   */
  static class DefaultEncapsulatedBuilder<BEAN extends IBean<BEAN>> implements EncapsulatedBuilder.IContainerEncapsulatedBuilder<BEAN>
  {
    private final List<BEAN> beanList;

    DefaultEncapsulatedBuilder(Iterable<BEAN> pBeans)
    {
      beanList = new ArrayList<>();
      pBeans.forEach(beanList::add);
    }

    @Override
    public void addBean(BEAN pBean, int pIndex)
    {
      beanList.add(pIndex, pBean);
    }

    @Override
    public boolean removeBean(BEAN pBean)
    {
      return beanList.remove(pBean);
    }

    @Override
    public BEAN removeBean(int pIndex)
    {
      return beanList.remove(pIndex);
    }

    @Override
    public BEAN getBean(int pIndex)
    {
      return beanList.get(pIndex);
    }

    @Override
    public int indexOfBean(BEAN pBean)
    {
      return beanList.indexOf(pBean);
    }

    @Override
    public int size()
    {
      return beanList.size();
    }

    @Override
    public void sort(Comparator<BEAN> pComparator)
    {
      beanList.sort(pComparator);
    }

    @NotNull
    @Override
    public Iterator<BEAN> iterator()
    {
      return beanList.iterator();
    }
  }
}
