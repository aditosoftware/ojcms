package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.datasource.IBeanContainerDataSource;
import de.adito.ojcms.beans.statistics.IStatisticData;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

/**
 * Encapsulated bean container data core.
 * It allows index based access.
 *
 * @param <BEAN> the type of the beans in this container data core
 * @author Simon Danner, 25.01.2017
 */
@EncapsulatedData
interface IEncapsulatedBeanContainerData<BEAN extends IBean<BEAN>> extends IEncapsulatedData<BEAN, IBeanContainerDataSource<BEAN>>
{
  /**
   * The type of the beans in this data core.
   *
   * @return a bean type
   */
  Class<BEAN> getBeanType();

  /**
   * Adds a bean at a certain index.
   *
   * @param pBean  the bean to add
   * @param pIndex the index
   */
  void addBean(BEAN pBean, int pIndex);

  /**
   * Replaces a bean at a certain index.
   *
   * @param pReplacement the bean that replaces the old one
   * @param pIndex       the index where the bean should be replaced
   * @return the replaced bean
   * @throws IndexOutOfBoundsException if, the index is not within the range of the contained beans
   */
  BEAN replaceBean(BEAN pReplacement, int pIndex);

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
   * @return the removed bean or null
   * @throws IndexOutOfBoundsException if, the index is not within the range of the contained beans
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
   * Returns the index of a certain bean within the data core.
   * -1, if the bean is not present within the data core.
   *
   * @param pBean the bean
   * @return the index of the bean (or -1 if not present)
   */
  int indexOfBean(BEAN pBean);

  /**
   * The amount of beans.
   *
   * @return the number of beans in the data core
   */
  int size();

  /**
   * Sorts this data core according to a given comparator.
   *
   * @param pComparator the comparator
   */
  void sort(Comparator<BEAN> pComparator);

  /**
   * Defines a limit (=amount of beans) for this container data core.
   * If the number of beans exceeds the limit, beans will be removed from the beginning of this container until the limit is reached.
   *
   * @param pMaxCount the maximum amount of beans in this core
   * @param pEvicting <tt>true</tt>, if the eldest beans should be removed, when the limit is reached
   */
  void setLimit(int pMaxCount, boolean pEvicting);

  /**
   * The statistic data of this core. (null if not present)
   *
   * @return statistic bean amount data
   */
  @Nullable
  IStatisticData<Integer> getStatisticData();
}
