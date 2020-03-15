package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;

import java.util.*;
import java.util.stream.*;

/**
 * A default implementation of the bean container interface.
 * It stores the encapsulated data core.
 * Instances can be created via static methods in {@link IBeanContainer}.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 23.08.2016
 */
@RequiresEncapsulatedAccess
public class BeanContainer<BEAN extends IBean> implements IBeanContainer<BEAN>
{
  private final IEncapsulatedBeanContainerData<BEAN> encapsulatedData;

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
   * Creates a new bean container based on a custom data source.
   *
   * @param pBeanType   the type of the beans in the container
   * @param pDataSource the data source of the container
   */
  protected BeanContainer(Class<BEAN> pBeanType, IBeanContainerDataSource<BEAN> pDataSource)
  {
    encapsulatedData = new EncapsulatedBeanContainerData<>(Objects.requireNonNull(pDataSource), Objects.requireNonNull(pBeanType));
  }

  @Override
  public IEncapsulatedBeanContainerData<BEAN> getEncapsulatedData()
  {
    return encapsulatedData;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{beanType: " + getBeanType().getSimpleName() + ", count: " + size() + "}\nbeans:\n" +
        stream()
            .map(Objects::toString)
            .collect(Collectors.joining("\n"));
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;

    if (!(pOther instanceof BeanContainer))
      return false;

    //noinspection unchecked
    final BeanContainer<BEAN> otherContainer = (BeanContainer<BEAN>) pOther;
    final int size = size();
    return size == otherContainer.size() &&
        IntStream.range(0, size).allMatch(pIndex -> Objects.equals(getBean(pIndex), otherContainer.getBean(pIndex)));
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(stream().toArray(Object[]::new));
  }
}
