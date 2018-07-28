package de.adito.beans.core.util.weak;

import java.util.Objects;
import java.util.stream.*;

/**
 * A collection of elements sorted by their input order.
 * Null elements are not allowed.
 * Duplicates are not allowed.
 *
 * @param <ELEMENT> the type of the elements
 * @author Simon Danner, 07.03.2018
 */
public interface IInputSortedElements<ELEMENT> extends Iterable<ELEMENT>
{
  /**
   * Adds a element.
   * It is not allowed to add null elements or duplicates.
   *
   * @param pElement the element to add
   * @return <tt>true</tt>, if the element has been added successfully
   * @throws IllegalArgumentException if, the element is null
   */
  boolean add(ELEMENT pElement);

  /**
   * Removes a certain element.
   *
   * @param pElement the element to remove
   * @return <tt>true</tt>, if the element has been removed successfully
   * @throws IllegalArgumentException if, the element to remove is null
   */
  boolean remove(ELEMENT pElement);

  /**
   * Removes all elements from the collection.
   */
  void clear();

  /**
   * The amount of elements in the collection.
   *
   * @return the amount of elements
   */
  int size();

  /**
   * Determines, if this collection is empty.
   *
   * @return <tt>true</tt>, if the collection is empty
   */
  default boolean isEmpty()
  {
    return size() == 0;
  }

  /**
   * Determines, if a certain element is contained.
   * Null elements are not allowed to check.
   *
   * @param pElement the element to check
   * @return <tt>true</tt>, if the element is contained
   * @throws IllegalArgumentException if, the element to check is null
   */
  default boolean contains(ELEMENT pElement)
  {
    if (pElement == null)
      throw new IllegalArgumentException("null elements are not allowed!");
    return stream().anyMatch(pContainedElement -> Objects.equals(pElement, pContainedElement));
  }

  /**
   * A stream of all elements in this collections.
   *
   * @return a stream of elements
   */
  default Stream<ELEMENT> stream()
  {
    return StreamSupport.stream(spliterator(), false);
  }
}
