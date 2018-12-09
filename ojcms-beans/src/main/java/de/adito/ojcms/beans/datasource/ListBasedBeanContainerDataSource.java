package de.adito.ojcms.beans.datasource;

import de.adito.ojcms.beans.IBean;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * A list based implementation of a {@link IBeanContainerDataSource}.
 * Used as default data source for bean containers.
 *
 * @param <BEAN> the type of the beans in the data source
 * @author Simon Danner, 08.12.2018
 */
public final class ListBasedBeanContainerDataSource<BEAN extends IBean<BEAN>> implements IBeanContainerDataSource<BEAN>
{
  private final List<BEAN> beanList;

  /**
   * Creates the list base data source.
   *
   * @param pBeans an initial amount of beans for the data source
   */
  public ListBasedBeanContainerDataSource(Iterable<BEAN> pBeans)
  {
    beanList = StreamSupport.stream(Objects.requireNonNull(pBeans).spliterator(), false)
        .collect(Collectors.toList());
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
