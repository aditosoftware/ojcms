package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;

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
@RequiresEncapsulatedAccess
public class BeanContainer<BEAN extends IBean<BEAN>> implements IBeanContainer<BEAN>
{
  private final Class<BEAN> beanType;
  private IEncapsulatedBeanContainerData<BEAN> encapsulated;

  /**
   * Creates an empty bean container.
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
    this(pBeanType, new ListBasedBeanContainerDataSource<>(pBeans));
  }

  /**
   * Creates a new bean container based on a data source.
   *
   * @param pBeanType   the type of the beans in the container
   * @param pDataSource the data source of the container
   */
  protected BeanContainer(Class<BEAN> pBeanType, IBeanContainerDataSource<BEAN> pDataSource)
  {
    beanType = Objects.requireNonNull(pBeanType);
    setEncapsulatedDataSource(Objects.requireNonNull(pDataSource));
  }

  @Override
  public IEncapsulatedBeanContainerData<BEAN> getEncapsulatedData()
  {
    return encapsulated;
  }

  @Override
  public void setEncapsulatedDataSource(IBeanContainerDataSource<BEAN> pDataSource)
  {
    encapsulated = new EncapsulatedBeanContainerData<>(pDataSource, beanType);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{beanType: " + getBeanType().getSimpleName() + ", count: " + size() + "}\nbeans:\n" +
        stream()
            .map(Objects::toString)
            .collect(Collectors.joining("\n"));
  }
}
