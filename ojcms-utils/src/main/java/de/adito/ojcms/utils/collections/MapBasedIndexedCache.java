package de.adito.ojcms.utils.collections;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Map based implementation of {@link IIndexedCache}.
 * Index and element access is always hash table based.
 *
 * @author Simon Danner, 10.01.2020
 */
public class MapBasedIndexedCache<T> implements IIndexedCache<T>
{
  private final int maxSize;
  private long currentId;
  private final Map<Long, T> contentById = new TreeMap<>();
  private final Map<T, Long> idByContent = new HashMap<>();
  private final Map<Long, Integer> indexById = new TreeMap<>();
  private final Map<Integer, Long> idByIndex = new TreeMap<>();

  /**
   * Initializes the indexed cached with no size limit.
   */
  public MapBasedIndexedCache()
  {
    this(-1);
  }

  /**
   * Initializes the indexed cache with a size limit.
   *
   * @param pMaxSize the maximum size for this cache
   */
  public MapBasedIndexedCache(int pMaxSize)
  {
    maxSize = pMaxSize;
  }

  @Override
  public boolean containsIndex(int pIndex)
  {
    return idByIndex.containsKey(_checkIndex(pIndex));
  }

  @Override
  public Optional<T> getElementAtIndex(int pIndex)
  {
    return Optional.ofNullable(idByIndex.get(_checkIndex(pIndex))).map(contentById::get);
  }

  @Override
  public OptionalInt indexOf(T pElement)
  {
    if (pElement == null || !idByContent.containsKey(pElement))
      return OptionalInt.empty();

    final Long id = idByContent.get(pElement);
    return OptionalInt.of(indexById.get(id));
  }

  @Override
  public void addAtIndex(T pElement, int pIndex)
  {
    Objects.requireNonNull(pElement, "The element to add must not be null!");
    _checkIndex(pIndex);

    final OptionalInt maxSize = getMaxSize();
    if (maxSize.isPresent() && maxSize.getAsInt() == pIndex + 1 && getElementAtIndex(pIndex).isPresent())
      throw new IllegalArgumentException("Unable to add new element at last possible index! max size: " + maxSize.getAsInt());

    if (idByIndex.containsKey(pIndex))
      _adaptIndexesAfterAddition(pIndex);

    idByIndex.put(pIndex, currentId);
    indexById.put(currentId, pIndex);
    contentById.put(currentId, pElement);

    if (idByContent.containsKey(pElement))
      throw new IllegalArgumentException("Duplicate content! That is forbidden! Duplicate: " + pElement);
    idByContent.put(pElement, currentId);

    currentId++;
    _assureConsistency();
  }

  @Override
  public Optional<T> replaceAtIndex(T pElement, int pIndex)
  {
    Objects.requireNonNull(pElement, "The new element to replace an existing with must not be null!");
    final Optional<T> oldElement = getElementAtIndex((_checkIndex(pIndex)));

    if (!oldElement.isPresent())
      addAtIndex(pElement, pIndex);
    else
    {
      final Long id = idByIndex.get(pIndex);
      contentById.put(id, pElement);
      idByContent.remove(oldElement.get());
      idByContent.put(pElement, id);
    }

    _assureConsistency();
    return oldElement;
  }

  @Override
  public Optional<T> removeAtIndex(int pIndex)
  {
    if (!idByIndex.containsKey(_checkIndex(pIndex)))
      return Optional.empty();

    final Long id = idByIndex.remove(pIndex);
    final T removedContent = contentById.remove(id);
    indexById.remove(id);
    idByContent.remove(removedContent);

    _adaptIndexesAfterRemoval(pIndex);
    _assureConsistency();
    return Optional.of(removedContent);
  }

  @Override
  public boolean removeElement(T pElement)
  {
    if (pElement == null || !idByContent.containsKey(pElement))
      return false;

    final Long id = idByContent.remove(pElement);
    final Integer index = indexById.remove(id);
    idByIndex.remove(index);
    contentById.remove(id);

    _adaptIndexesAfterRemoval(index);
    _assureConsistency();
    return true;
  }

  @Override
  public void sortElements(Comparator<T> pComparator)
  {
    indexById.clear();
    idByIndex.clear();

    final List<T> listOfElements = new ArrayList<>(contentById.values());
    listOfElements.sort(pComparator);

    for (int index = 0; index < listOfElements.size(); index++)
    {
      final T element = listOfElements.get(index);
      final Long id = idByContent.get(element);
      indexById.put(id, index);
      idByIndex.put(index, id);
    }

    _assureConsistency();
  }

  @Override
  public void clear()
  {
    contentById.clear();
    idByContent.clear();
    indexById.clear();
    idByIndex.clear();
  }

  @Override
  public int size()
  {
    return contentById.size();
  }

  @Override
  public OptionalInt getMaxSize()
  {
    return maxSize < 0 ? OptionalInt.empty() : OptionalInt.of(maxSize);
  }

  /**
   * Adapts all indexes above an affected index after an addition of an element.
   * All indexes will be increased by one.
   *
   * @param pAffectedIndex the affected index of the addition
   */
  private void _adaptIndexesAfterAddition(int pAffectedIndex)
  {
    final Set<Integer> indexesToRemove = new HashSet<>();

    indexById.entrySet().stream() //
        .filter(pEntry -> pEntry.getValue() >= pAffectedIndex) //Remove entries with index below affected one
        .sorted((pEntry1, pEntry2) -> pEntry2.getValue() - pEntry1.getValue()) //Descending by index
        .forEach(pEntry ->
        {
          final int oldIndex = pEntry.getValue();
          final int newIndex = oldIndex + 1;

          pEntry.setValue(newIndex);
          idByIndex.put(newIndex, pEntry.getKey());
          indexesToRemove.add(oldIndex);
          indexesToRemove.remove(newIndex);
        });

    indexesToRemove.forEach(idByIndex::remove);
  }

  /**
   * Adapts all indexes above an affected index after a removal of an element.
   * All indexes will be decreased by one.
   *
   * @param pAffectedIndex the affected index of the removal
   */
  private void _adaptIndexesAfterRemoval(int pAffectedIndex)
  {
    final Set<Integer> indexesToRemove = new HashSet<>();

    indexById.entrySet().stream() //
        .filter(pEntry -> pEntry.getValue() > pAffectedIndex) //Remove entries with index below the affected one
        .sorted((pEntry1, pEntry2) -> pEntry2.getValue() - pEntry1.getValue()) //Descending by index
        .forEach(pEntry ->
        {
          final int oldIndex = pEntry.getValue();
          final int newIndex = oldIndex - 1;

          pEntry.setValue(newIndex);
          idByIndex.put(newIndex, pEntry.getKey());
          indexesToRemove.add(oldIndex);
          indexesToRemove.remove(newIndex);
        });

    indexesToRemove.forEach(idByIndex::remove);
  }

  /**
   * Checks if a given index is greater than zero and within the bounds of the max size of the cache.
   * Throws an {@link IndexOutOfBoundsException} for illegal indexes.
   *
   * @param pIndex the index to check
   * @return the checked index
   */
  private int _checkIndex(int pIndex)
  {
    if (pIndex < 0)
      throw new IndexOutOfBoundsException("Index is smaller than zero! index: " + pIndex);

    final OptionalInt maxSize = getMaxSize();
    if (maxSize.isPresent() && pIndex >= maxSize.getAsInt())
      throw new IndexOutOfBoundsException("Index is greater than max size! index: " + pIndex + ", max size: " + maxSize.getAsInt());

    return pIndex;
  }

  /**
   * Makes sure the content of the indexed is still consistent. (May be removed if the implementation is stable enough)
   */
  private void _assureConsistency()
  {
    final boolean isConsistent = IntStream.of(idByContent.size(), contentById.size(), indexById.size(), idByIndex.size()) //
        .distinct() //
        .count() == 1;

    if (!isConsistent)
      throw new IllegalStateException("Content of indexed cache is not consistent anymore!");
  }
}
