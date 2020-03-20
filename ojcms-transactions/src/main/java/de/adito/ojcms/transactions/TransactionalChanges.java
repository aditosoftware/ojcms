package de.adito.ojcms.transactions;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;
import de.adito.ojcms.transactions.api.*;
import de.adito.ojcms.transactions.spi.IBeanDataStorage;

import javax.annotation.*;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages bean related changes for a single transaction.
 *
 * @author Simon Danner, 27.12.2019
 */
@TransactionalScoped
class TransactionalChanges
{
  @Inject
  private OverallTransactionalChanges overallTransactionalChanges;
  @Inject
  private IBeanDataStorage storage;

  //Containers
  private final Map<String, Set<_VariableBeanAddition>> additionsByContainer = new HashMap<>();
  private final Map<String, Set<InitialIndexKey>> removalsByContainer = new HashMap<>();
  private final Map<InitialIndexKey, Map<IField<?>, Object>> changedContainerValuesByContainer = new HashMap<>();
  //Single beans
  private final Map<SingleBeanKey, Map<IField<?>, Object>> changedSingleBeanValues = new HashMap<>();

  /**
   * Evaluates the size difference of a bean container within the active transaction. This methods simply counts all additions
   * of the requested container and subtracts the removals from the resulting value.
   *
   * @param pContainerId the id of the container to evaluate the size difference for
   * @return zero for no size difference, a positive value for the number to increase the original size with,
   * a negative value to subtract from the original value
   */
  int getContainerSizeDifference(String pContainerId)
  {
    final int additions = _countIfNotNull(additionsByContainer.get(pContainerId));
    final int removals = _countIfNotNull(removalsByContainer.get(pContainerId));
    return additions - removals;
  }

  /**
   * Integrates bean value changes of the transaction into some {@link PersistentBeanData} from a container.
   * The index of the persistent data may be adapted if it has been changed within the transaction.
   *
   * @param pKey         the initial index based key to integrate changes for
   * @param pInitialData the initial data to integrate the changes of the transaction
   * @return the persistent bean data with integrated changes
   */
  PersistentBeanData integrateContainerBeanChanges(InitialIndexKey pKey, PersistentBeanData pInitialData)
  {
    final PersistentBeanData beanData = Optional.ofNullable(changedContainerValuesByContainer.get(pKey)) //
        .map(pInitialData::integrateChanges) //
        .orElse(pInitialData);

    final int currentIndex = initialToCurrentIndexKey(pKey).getIndex();
    return beanData.getIndex() != currentIndex ? new PersistentBeanData(currentIndex, beanData.getData()) : beanData;
  }

  /**
   * Integrates single bean value changes of the transaction into some {@link PersistentBeanData}.
   *
   * @param pKey         the single bean key to integrate changes for
   * @param pInitialData the initial data to integrate the changes of the transaction
   * @return the persistent bean data with integrated changes
   */
  PersistentBeanData integrateSingleBeanChanges(SingleBeanKey pKey, PersistentBeanData pInitialData)
  {
    return Optional.ofNullable(changedSingleBeanValues.get(pKey)) //
        .map(pInitialData::integrateChanges) //
        .orElse(pInitialData);
  }

  /**
   * Maybe changes the index of a {@link InitialIndexKey} that existed at the start of the transaction due to additions or removals.
   * If the index changes, a new key instance will be created.
   *
   * @param pInitialKey the initial key to check and maybe adapt
   * @return the potentially adapted bean index key
   */
  CurrentIndexKey initialToCurrentIndexKey(InitialIndexKey pInitialKey)
  {
    if (isRemoved(pInitialKey))
      throw new IllegalArgumentException("Unable to convert initial to current key if the bean has been deleted!");

    final String containerId = pInitialKey.getContainerId();
    final int initialIndex = pInitialKey.getIndex();
    final int indexAfterRemovals = initialIndex - _countRemovalsBeforeAndAtIndex(containerId, initialIndex);
    final int currentIndex = indexAfterRemovals + _countAdditionsBeforeIndex(containerId, indexAfterRemovals);
    return pInitialKey.toCurrentKey(currentIndex);
  }

  /**
   * Adapts the index of a {@link CurrentIndexKey} that may have been influence by additions or removals back to the transaction's initial
   * state. If the index changes, a new key instance will be created.
   *
   * @param pCurrentKey the key to check and maybe adapt
   * @return the potentially adapted bean index key
   */
  InitialIndexKey currentToInitialIndexKey(CurrentIndexKey pCurrentKey)
  {
    final String containerId = pCurrentKey.getContainerId();
    final int currentIndex = pCurrentKey.getIndex();
    final int indexAfterAdditions = currentIndex - _countAdditionsBeforeIndex(containerId, currentIndex);
    final int initialIndex = indexAfterAdditions + _countRemovalsBeforeAndAtIndex(containerId, indexAfterAdditions);
    return pCurrentKey.toInitialKey(initialIndex);
  }

  /**
   * Determines if a bean container is changed related to its size.
   *
   * @param pContainerId the id of the container to check
   * @return <tt>true</tt> if the container has been changed related to its size
   */
  boolean isContainerDirtyInSize(String pContainerId)
  {
    return additionsByContainer.containsKey(pContainerId) && !additionsByContainer.get(pContainerId).isEmpty() || //
        removalsByContainer.containsKey(pContainerId) && !removalsByContainer.get(pContainerId).isEmpty();
  }

  /**
   * Determines if specific bean data within a container has been changed.
   *
   * @param pKey the key identifying the bean within the container by index
   * @return <tt>true</tt> if the bean data has been changed
   */
  boolean isContainerBeanDirty(InitialIndexKey pKey)
  {
    return changedContainerValuesByContainer.containsKey(pKey);
  }

  /**
   * Determines if specific single bean data has been changed.
   *
   * @param pKey the key identifying the single bean
   * @return <tt>true</tt> if the bean data has been changed
   */
  boolean isSingleBeanDirty(SingleBeanKey pKey)
  {
    return changedSingleBeanValues.containsKey(pKey);
  }

  /**
   * Determines if a bean related to a {@link CurrentIndexKey} has been added within the transaction.
   *
   * @param pKey the index based key to check
   * @return <tt>true</tt> if the bean data has been added within the transaction
   */
  boolean isAdded(CurrentIndexKey pKey)
  {
    final String containerId = pKey.getContainerId();
    return additionsByContainer.containsKey(containerId) && //
        additionsByContainer.get(containerId).contains(new _VariableBeanAddition(pKey.getIndex()));
  }

  /**
   * Determines if a bean related to a {@link InitialIndexKey} has been removed within the transaction.
   *
   * @param pKey the index based key to check
   * @return <tt>true</tt> if the bean data has removed within the transaction
   */
  boolean isRemoved(InitialIndexKey pKey)
  {
    final String containerId = pKey.getContainerId();
    return removalsByContainer.containsKey(containerId) && removalsByContainer.get(containerId).contains(pKey);
  }

  /**
   * Notifies the change manager that a bean has been added to a container.
   *
   * @param pBeanAddition data describing the addition
   */
  void beanAdded(BeanAddition pBeanAddition)
  {
    final int index = pBeanAddition.getIndex();
    final String containerId = pBeanAddition.getContainerId();
    final _VariableBeanAddition addition = new _VariableBeanAddition(pBeanAddition);
    final Set<_VariableBeanAddition> additionsForContainer = additionsByContainer.computeIfAbsent(containerId, pId -> new HashSet<>());

    //Adapt indexes of prior additions for the same and the indexes above
    additionsForContainer.stream() //
        .filter(pAddition -> pAddition.getIndex() >= index) //
        .forEach(_VariableBeanAddition::incrementIndex);

    additionsForContainer.add(addition);
  }

  /**
   * Notifies the change manager that a bean has been removed from a container.
   *
   * @param pCurrentKey the removed key referring to the current state of the transaction
   */
  void beanRemoved(CurrentIndexKey pCurrentKey)
  {
    if (isAdded(pCurrentKey)) //If the bean has been added in the same transaction, just removed the change again
    {
      final Set<_VariableBeanAddition> additionsOfContainer = additionsByContainer.get(pCurrentKey.getContainerId());
      additionsOfContainer.remove(new _VariableBeanAddition(pCurrentKey.getIndex()));
      //Adapt keys of additions
      additionsOfContainer.stream().filter(pAddition -> pAddition.getIndex() > pCurrentKey.getIndex()) //
          .forEach(_VariableBeanAddition::decrementIndex);
    }
    else
    {
      final InitialIndexKey keyToRemove = currentToInitialIndexKey(pCurrentKey);
      removalsByContainer.computeIfAbsent(keyToRemove.getContainerId(), pId -> new HashSet<>()).add(keyToRemove);
    }
  }

  /**
   * Notifies the change manager that a value of a bean within a container has been changed.
   *
   * @param pCurrentKey   the key identifying the bean within the container by index
   * @param pChangedField the changed bean field
   * @param pNewValue     the new field value
   * @param <VALUE>       the data type of the changed field
   */
  <VALUE> void containerBeanValueHasChanged(CurrentIndexKey pCurrentKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    if (isAdded(pCurrentKey))
    {
      final _VariableBeanAddition addition = additionsByContainer.get(pCurrentKey.getContainerId()).stream() //
          .filter(pBeanAddition -> pBeanAddition.getIndex() == pCurrentKey.getIndex()) //
          .findAny() //
          .orElseThrow(AssertionError::new);

      //Add changed value to addition data
      addition.getContent().put(pChangedField, pNewValue);
    }
    else
    {
      final InitialIndexKey changedInitialKey = currentToInitialIndexKey(pCurrentKey);
      if (isRemoved(changedInitialKey))
        throw new IllegalArgumentException(
            "Cannot register change! Bean data for initial key " + changedInitialKey + " has been removed" + " within this transaction!");

      changedContainerValuesByContainer.computeIfAbsent(changedInitialKey, pChangeKey -> new HashMap<>()).put(pChangedField, pNewValue);
    }
  }

  /**
   * Notifies the change manager that a value of a single bean has been changed.
   *
   * @param pKey          the key identifying the single bean
   * @param pChangedField the changed bean field
   * @param pNewValue     the new field value
   * @param <VALUE>       the data type of the changed field
   */
  <VALUE> void singleBeanValueHasChanged(SingleBeanKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    changedSingleBeanValues.computeIfAbsent(pKey, key -> new HashMap<>()).put(pChangedField, pNewValue);
  }

  /**
   * Commits all changes made in this transaction to the persistent storage system.
   */
  void commitChanges()
  {
    //The order is very important here: removals first, then additions and value changes at the end
    storage.processRemovals(removalsByContainer);
    additionsByContainer.forEach((pContainerId, pAddition) -> storage.processAdditionsForContainer(pContainerId, pAddition.stream() //
        .map(_VariableBeanAddition::toBeanAdditionWithNewIndex) //
        .collect(Collectors.toSet())));

    changedContainerValuesByContainer.forEach((pKey, pValues) -> storage.processChangesForContainerBean(pKey, pValues));
    changedSingleBeanValues.forEach((pKey, pValues) -> storage.processChangesForSingleBean(pKey, pValues));
  }

  /**
   * Counts the number of additions for a specific container before a given index within the transaction.
   *
   * @param pContainerId the id of the container to count additions for
   * @param pIndex       the given index boundary
   * @return the number of additions before the given index
   */
  private int _countAdditionsBeforeIndex(String pContainerId, int pIndex)
  {
    return !additionsByContainer.containsKey(pContainerId) ? 0 : (int) additionsByContainer.get(pContainerId).stream() //
        .mapToInt(_VariableBeanAddition::getIndex) //
        .filter(pAddedIndex -> pAddedIndex < pIndex) //
        .count();
  }

  /**
   * Counts the number of removals for a specific container before and at a given index within the transaction.
   *
   * @param pContainerId the id of the container to count removals for
   * @param pIndex       the given index boundary
   * @return the number of removals before and at the given index
   */
  private int _countRemovalsBeforeAndAtIndex(String pContainerId, int pIndex)
  {
    return !removalsByContainer.containsKey(pContainerId) ? 0 : (int) removalsByContainer.get(pContainerId).stream() //
        .mapToInt(InitialIndexKey::getIndex) //
        .filter(pRemovedIndex -> pRemovedIndex <= pIndex) //
        .count();
  }

  /**
   * Counts the elements of a set. If the set is empty the result will be zero.
   *
   * @param pAnySet the set to count elements of
   * @return the count of elements in the set or zero if the set is null
   */
  private int _countIfNotNull(Set<?> pAnySet)
  {
    return Optional.ofNullable(pAnySet) //
        .map(Set::size) //
        .orElse(0);
  }

  /**
   * Registers this transactional changes instance at {@link OverallTransactionalChanges}.
   */
  @PostConstruct
  private void _register()
  {
    overallTransactionalChanges.registerFromTransaction(this);
  }

  /**
   * Deregisters this transactional changes instance from {@link OverallTransactionalChanges}.
   */
  @PreDestroy
  private void _deregister()
  {
    overallTransactionalChanges.deregister(this);
  }

  /**
   * Describes the addition of a bean within the transaction.
   * The index of added beans may be adapted within the transaction if beans get added or removed later on.
   */
  private static class _VariableBeanAddition
  {
    private final BeanAddition original;
    private final Map<IField<?>, Object> content;
    private int index;

    /**
     * Creates an empty addition instance for comparisons.
     *
     * @param pIndex the index for the bean addition
     */
    _VariableBeanAddition(int pIndex)
    {
      original = null;
      content = null;
      index = pIndex;
    }

    /**
     * Creates a new bean addition for given type, data and index.
     *
     * @param pBeanAddition data describing the addition
     */
    _VariableBeanAddition(BeanAddition pBeanAddition)
    {
      original = pBeanAddition;
      content = pBeanAddition.getData();
      index = pBeanAddition.getIndex();
    }

    /**
     * Return either the original {@link BeanAddition} if the index didn't change or creates a copy with the new index.
     *
     * @return the bean addition with a potential adapted index
     */
    BeanAddition toBeanAdditionWithNewIndex()
    {
      return original.getIndex() == index && original.getData().equals(content) ? original : //
          new BeanAddition(index, content, original.getBeanType(), original.getContainerId());
    }

    /**
     * The content of the added bean.
     */
    Map<IField<?>, Object> getContent()
    {
      return content;
    }

    /**
     * The index the bean has been added.
     */
    int getIndex()
    {
      return index;
    }

    /**
     * Increments the index of the added bean by one.
     */
    void incrementIndex()
    {
      index++;
    }

    /**
     * Decrements the index of the added bean by one.
     */
    void decrementIndex()
    {
      index--;
    }

    @Override
    public boolean equals(Object pOther)
    {
      if (this == pOther)
        return true;
      if (pOther == null || getClass() != pOther.getClass())
        return false;

      final _VariableBeanAddition that = (_VariableBeanAddition) pOther;
      return index == that.index;
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(index);
    }
  }
}
