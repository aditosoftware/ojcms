package de.adito.ojcms.utils.collections;

import java.util.*;
import java.util.function.Function;

/**
 * Indexed based, sortable cache that may be filled lazily. Also adapts indexes when elements are removed or added.
 * Null elements are not allowed.
 *
 * @param <T> the type of the elements within the indexed cache
 * @author Simon Danner, 10.01.2020
 */
public interface IIndexedCache<T>
{
  /**
   * Determines if the cache contains an element at a specific index yet.
   *
   * @param pIndex the index to check
   * @return <tt>true</tt> if the element behind the requested index has been cached already
   */
  boolean containsIndex(int pIndex);

  /**
   * Tries to resolve a cached element at a specific index.
   *
   * @param pIndex the index to retrieve the element from
   * @return an optionally cached element
   */
  Optional<T> getElementAtIndex(int pIndex);

  /**
   * Tries to resolve the index of a specific element within the cache.
   * The result is empty if the element hasn't been cached yet.
   *
   * @param pElement the element to determine the index for
   * @return an optional index
   */
  OptionalInt indexOf(T pElement);

  /**
   * Registers a cached element at a specific index. If the index is not available anymore, the element is considered as
   * a new addition to the cache. In this case all elements that are above the added (index-wise) will increase their index by one.
   *
   * @param pElement the element to cache
   * @param pIndex   the index to cache the element at
   */
  void addAtIndex(T pElement, int pIndex);

  /**
   * Replaces an existing element at a specific index.
   *
   * @param pElement the new element to replace the prior with
   * @param pIndex   the index at which the element should be replaced
   * @return the optional prior element at the requested index
   */
  Optional<T> replaceAtIndex(T pElement, int pIndex);

  /**
   * Creates a new element at a specific index if there is no element at the requested index already.
   * Then returns either the created or the existing element.
   *
   * @param pIndex   the index the resolve the element from
   * @param pCreator a function creating the new element if it does not existing beforehand
   * @return the (maybe created) element at the requested index
   */
  default T computeIfAbsent(int pIndex, Function<Integer, T> pCreator)
  {
    return getElementAtIndex(pIndex)
        .orElseGet(() ->
                   {
                     final T createdElement = pCreator.apply(pIndex);
                     addAtIndex(createdElement, pIndex);
                     return createdElement;
                   });
  }

  /**
   * Tries to remove an element at a specific index from the cache.
   * Decreases all indexes above the affected one.
   *
   * @param pIndex the index to remove the element from
   * @return the optionally removed element
   */
  Optional<T> removeAtIndex(int pIndex);

  /**
   * Removes a specific element from the cache. The identification is equals/hashCode based.
   * Decreases all indexes above the affected element.
   *
   * @param pElement the element to remove
   * @return <tt>true</tt> if the element has been removed
   */
  boolean removeElement(T pElement);

  /**
   * Sorts the cache based on a given {@link Comparator}.
   * The cache discards all index information, takes all cached elements, sorts them by the comparator and registers
   * new indexes (starting from zero) for the resulting order.
   *
   * @param pComparator the comparator to sort the elements with
   */
  void sortElements(Comparator<T> pComparator);

  /**
   * Clears the cache. A potentially given maximum size information will remain.
   */
  void clear();

  /**
   * The current amount of cached elements.
   *
   * @return the current size of the cache
   */
  int size();

  /**
   * An optionally given max size of the cache.
   *
   * @return the optional max size
   */
  OptionalInt getMaxSize();

  /**
   * Determines if the cache is empty.
   *
   * @return <tt>true</tt> if the number of cached elements is zero.
   */
  default boolean isEmpty()
  {
    return size() == 0;
  }

  /**
   * Determines if the cache is full based on its max size.
   * If there is no maximum size given, the cache is never full.
   *
   * @return <tt>true</tt> if the cache is full
   */
  default boolean isFull()
  {
    final OptionalInt maxIndex = getMaxSize();
    return !maxIndex.isPresent() || maxIndex.getAsInt() == size();
  }
}
