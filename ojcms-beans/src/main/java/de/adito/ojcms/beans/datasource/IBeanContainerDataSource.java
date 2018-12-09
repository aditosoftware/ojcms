package de.adito.ojcms.beans.datasource;

import de.adito.ojcms.beans.IBean;

import java.util.Comparator;

/**
 * Data source for a bean container.
 * Used to create encapsulated data cores.
 *
 * @param <BEAN> the type of the beans of this data source
 * @author Simon Danner, 08.12.2018
 */
public interface IBeanContainerDataSource<BEAN extends IBean<BEAN>> extends IDataSource, Iterable<BEAN>
{
  /**
   * Adds a bean at a certain index.
   *
   * @param pBean  the bean to add
   * @param pIndex the index
   */
  void addBean(BEAN pBean, int pIndex);

  /**
   * Removes the first occurrence of a certain bean.
   *
   * @param pBean the bean to remove
   * @return <tt>true</tt>, if the bean has been removed successfully
   */
  boolean removeBean(BEAN pBean);

  /**
   * Removes a bean by index.
   *
   * @param pIndex the index to remove
   * @return the removed bean
   */
  BEAN removeBean(int pIndex);

  /**
   * Returns a bean by its index.
   *
   * @param pIndex the index of the bean
   * @return the bean at the certain index
   */
  BEAN getBean(int pIndex);

  /**
   * Returns the index of a certain bean within the container source.
   * -1, if the bean is not present within the core.
   *
   * @param pBean the bean
   * @return the index of the bean
   */
  int indexOfBean(BEAN pBean);

  /**
   * The amount of beans in this data source.
   *
   * @return the amount of beans
   */
  int size();

  /**
   * Sorts this data source according to a given comparator.
   *
   * @param pComparator the comparator
   */
  void sort(Comparator<BEAN> pComparator);
}
