package de.adito.ojcms.utils.collections;

import java.util.*;

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
    return Optional.ofNullable(idByIndex.get(_checkIndex(pIndex)))
        .map(contentById::get);
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
      _adaptIndexesAfterContentChange(pIndex, true);

    idByIndex.put(pIndex, currentId);
    indexById.put(currentId, pIndex);
    contentById.put(currentId, pElement);
    idByContent.put(pElement, currentId);
    currentId++;
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
      idByContent.remove(pElement);
      idByContent.put(pElement, id);
    }

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

    _adaptIndexesAfterContentChange(pIndex, false);
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

    _adaptIndexesAfterContentChange(index, false);
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
   * Adapts all indexes above an affected index after an addition or removal of an element.
   * In case of an addition all indexes will be increased by one.
   * Accordingly in case of a removal all indexes above will be decreased by one.
   *
   * @param pAffectedIndex the affected index of the addition or removal
   * @param pIsAddition    <tt>true</tt> if the change has been an addition
   */
  private void _adaptIndexesAfterContentChange(int pAffectedIndex, boolean pIsAddition)
  {
    final int threshold = pAffectedIndex + (pIsAddition ? 0 : 1);

    for (Map.Entry<Long, Integer> entry : indexById.entrySet())
    {
      if (entry.getValue() < threshold)
        continue;

      final int oldIndex = entry.getValue();
      final int newIndex = pIsAddition ? oldIndex + 1 : oldIndex - 1;

      entry.setValue(newIndex);
      idByIndex.remove(oldIndex);
      idByIndex.put(newIndex, entry.getKey());
    }
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
}
