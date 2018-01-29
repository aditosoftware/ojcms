package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanContainerChangeListener;
import de.adito.beans.core.statistics.IStatisticData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Defines the data core for a bean container.
 * It is based on the List interface to additionally provide the ability to access beans by their indices.
 *
 * @param <BEAN> the type of the beans in this container
 * @author Simon Danner, 25.01.2017
 */
interface IBeanContainerEncapsulated<BEAN extends IBean<BEAN>> extends IEncapsulated<BEAN, BEAN, IBeanContainerChangeListener<BEAN>>, List<BEAN>
{
  /**
   * The type of the beans in this container.
   */
  Class<BEAN> getBeanType();

  /**
   * Defines a limit (=amount of beans) for this container data core.
   *
   * @param pMaxCount the maximum amount of beans in this core
   * @param pEvicting <tt>true</tt>, if the eldest beans should be removed, when the limit is reached
   */
  void setLimit(int pMaxCount, boolean pEvicting);

  /**
   * The statistic data of this core. (null if not present)
   */
  @Nullable
  IStatisticData<Integer> getStatisticData();

  @Override
  default Stream<BEAN> stream()
  {
    return IEncapsulated.super.stream();
  }

  @Override
  default Stream<BEAN> parallelStream()
  {
    return IEncapsulated.super.parallelStream();
  }
}
