package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.datasource.IBeanContainerDataSource;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.utils.IndexBasedIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A {@link IBeanContainerDataSource} for persistent bean containers.
 * The actual persistent beans within the container are provided by a {@link TransactionalScoped} {@link ContainerContent}.
 *
 * @param <BEAN> the type of the beans in the container source
 * @author Simon Danner, 30.12.2019
 */
class PersistentContainerDatasource<BEAN extends IBean> implements IBeanContainerDataSource<BEAN>
{
  private final ContainerContent<BEAN> content; //Transactional scoped

  /**
   * Initializes the persistent container datasource.
   *
   * @param pContent the transactional scoped content provider
   */
  PersistentContainerDatasource(ContainerContent<BEAN> pContent)
  {
    content = pContent;
  }

  @Override
  public void addBean(BEAN pBean, int pIndex)
  {
    content.addBean(pBean, pIndex);
  }

  @Override
  public boolean removeBean(BEAN pBean)
  {
    return content.removeBean(pBean);
  }

  @Override
  public BEAN removeBean(int pIndex)
  {
    return content.removeBean(pIndex);
  }

  @Override
  public BEAN getBean(int pIndex)
  {
    return content.getBean(pIndex);
  }

  @Override
  public int indexOfBean(BEAN pBean)
  {
    return content.indexOf(pBean);
  }

  @Override
  public int size()
  {
    return content.size();
  }

  @Override
  public void sort(Comparator<BEAN> pComparator)
  {
    throw new UnsupportedOperationException("Sorting not supported for persistent containers!");
  }

  @NotNull
  @Override
  public Iterator<BEAN> iterator()
  {
    content.requiresFullLoad();
    return IndexBasedIterator.buildIterator(this::getBean, this::size)
        .withRemover(this::removeBean)
        .createIterator();
  }
}
