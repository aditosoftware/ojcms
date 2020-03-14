package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.utils.StringUtility;
import de.adito.ojcms.utils.collections.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.adito.ojcms.persistence.datasource.BeanPersistenceUtil.newPersistentBeanInstance;

/**
 * Manages the content of a persistent bean container within one {@link ITransaction}.
 *
 * @author Simon Danner, 30.12.2019
 */
class ContainerContent<BEAN extends IBean<BEAN>>
{
  private final String containerId;
  private final Class<BEAN> beanType;
  private final ITransaction transaction;
  private final IIndexedCache<BEAN> content = new MapBasedIndexedCache<>();

  /**
   * Initializes the container content.
   *
   * @param pContainerId the id of the persistent container the content belongs to
   * @param pBeanType    the bean type of the container
   * @param pTransaction the transaction the content is associated with
   */
  ContainerContent(String pContainerId, Class<BEAN> pBeanType, ITransaction pTransaction)
  {
    containerId = StringUtility.requireNotEmpty(pContainerId, "container id");
    beanType = Objects.requireNonNull(pBeanType);
    transaction = Objects.requireNonNull(pTransaction);
  }

  /**
   * Requests the size of the persistent bean container.
   *
   * @return the size of the container
   */
  int size()
  {
    return transaction.requestContainerSize(containerId);
  }

  /**
   * Resolves the bean at a specific index.
   *
   * @param pIndex the index of the bean to resolve
   * @return the bean at the given index
   */
  BEAN getBean(int pIndex)
  {
    return _getOrCreateBean(_checkIndex(pIndex), () -> _beanContentForIndex(pIndex));
  }

  /**
   * Resolves the index of a bean.
   *
   * @param pBean the bean to resolve the index for
   * @return the index of the requested bean or -1 if not present
   */
  int indexOf(BEAN pBean)
  {
    final OptionalInt indexFromCachedContent = content.indexOf(pBean);

    if (indexFromCachedContent.isPresent())
      return indexFromCachedContent.getAsInt();

    if (_isFullyLoaded())
      return -1;

    return _loadIndexOfBean(pBean).orElse(-1);
  }

  /**
   * Removes a bean at a specific index.
   *
   * @param pIndex the index to remove the bean from
   * @return the removed bean instance
   */
  BEAN removeBean(int pIndex)
  {
    _checkIndex(pIndex);

    final BEAN removedBean = content.removeAtIndex(pIndex)
        .orElseGet(() -> newPersistentBeanInstance(beanType, new PersistentBeanDatasource(_beanContentForIndex(pIndex))))
        .useDefaultEncapsulatedDataSource();

    transaction.registerBeanRemoval(_indexKey(pIndex));
    return removedBean;
  }

  /**
   * Removes a specific bean from the container.
   *
   * @param pBean the bean instance to remove
   * @return <tt>true</tt> if the bean has been removed
   */
  boolean removeBean(BEAN pBean)
  {
    final int index = indexOf(pBean);

    if (index == -1)
      return false;

    content.removeElement(pBean);
    pBean.useDefaultEncapsulatedDataSource();
    transaction.registerBeanRemoval(_indexKey(index));
    return true;
  }

  /**
   * Adds a bean at a specific index to the container.
   *
   * @param pBean  the bean to add
   * @param pIndex the index to add the bean at
   */
  void addBean(BEAN pBean, int pIndex)
  {
    pBean.setEncapsulatedDataSource(new PersistentBeanDatasource(_beanContentForIndexAndContent(pIndex, pBean.toMap())));
    content.addAtIndex(pBean, pIndex);
    transaction.registerBeanAddition(_indexKey(pIndex), pBean.toMap());
  }

  /**
   * Should be called by methods that expect the bean data for the container to be fully loaded.
   * If not every bean has been loaded yet, a full data load will be performed.
   */
  void requiresFullLoad()
  {
    if (_isFullyLoaded())
      return;

    transaction.requestFullContainerLoad(containerId).entrySet().stream()
        .filter(pEntry -> !content.containsIndex(pEntry.getKey()))
        .forEach(pEntry -> _getOrCreateBean(pEntry.getKey(), () ->
            _beanContentForIndexAndContent(pEntry.getKey(), pEntry.getValue().getData())));
  }

  /**
   * Requests the index of a bean from the {@link ITransaction}.
   *
   * @param pBean the bean to request the index for
   * @return an optionally resolved index
   */
  private OptionalInt _loadIndexOfBean(BEAN pBean)
  {
    final Map<IField<?>, Object> identifiers = pBean.getIdentifiers().stream()
        .collect(Collectors.toMap(FieldValueTuple::getField, FieldValueTuple::getValue));

    final Optional<PersistentBeanData> beanData = transaction.requestBeanDataByIdentifierTuples(containerId, identifiers);

    if (!beanData.isPresent())
      return OptionalInt.empty();

    final int index = beanData.get().getIndex();
    //Add bean to the content map if we requested the data anyway
    _getOrCreateBean(index, () -> _beanContentForIndexAndContent(index, beanData.get().getData()));

    return OptionalInt.of(index);
  }

  /**
   * Resolves a bean instance by id or creates a new instance if not present yet.
   *
   * @param pIndex              the index of the bean to resolve
   * @param pBeanContentCreator a supplier for a new {@link AbstractBeanContent} to create a new bean instance from
   * @return the resolved or created bean instance
   */
  private BEAN _getOrCreateBean(int pIndex, Supplier<AbstractBeanContent> pBeanContentCreator)
  {
    return content.computeIfAbsent(pIndex, index ->
        newPersistentBeanInstance(beanType, new PersistentBeanDatasource(pBeanContentCreator.get())));
  }

  /**
   * Checks if a given index is within its bound.
   * Throws an {@link IndexOutOfBoundsException} otherwise.
   *
   * @param pIndex the index to check
   * @return the successfully checked index
   */
  private int _checkIndex(int pIndex)
  {
    if (pIndex < 0 || pIndex >= size())
      throw new IndexOutOfBoundsException("Bad index: " + pIndex);

    return pIndex;
  }

  /**
   * Determines if the content is fully loaded for the associated transaction.
   *
   * @return <tt>true</tt> if the content has been fully loaded for the transaction
   */
  private boolean _isFullyLoaded()
  {
    return size() == content.size();
  }

  /**
   * Creates an instance of {@link BeanContentForContainer} that is based on a {@link CurrentIndexKey}.
   *
   * @param pIndex the index the bean to create the instance for is located within the bean container
   * @return the created bean content by index
   */
  private BeanContentForContainer _beanContentForIndex(int pIndex)
  {
    return new BeanContentForContainer(_indexKey(pIndex), transaction);
  }

  /**
   * Creates an instance of {@link BeanContentForContainer} that is based on a {@link CurrentIndexKey} with a given content.
   *
   * @param pIndex   the index the bean to create the instance for is located within the bean container
   * @param pContent the preset content for the instance to create
   * @return the created bean content by index
   */
  private BeanContentForContainer _beanContentForIndexAndContent(int pIndex, Map<IField<?>, Object> pContent)
  {
    return new BeanContentForContainer(_indexKey(pIndex), transaction, pContent);
  }

  /**
   * Creates a new {@link CurrentIndexKey} for a given index and the container id of this content instance.
   *
   * @param pIndex the index to create the key for
   * @return the created bean index key
   */
  private CurrentIndexKey _indexKey(int pIndex)
  {
    return new CurrentIndexKey(containerId, pIndex);
  }
}
