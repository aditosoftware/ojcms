package de.adito.beans.core.util.weak;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A weak reference container for any elements sorted by their input order.
 *
 * @param <ELEMENT> the type of the elements in this container
 * @author Simon Danner, 07.03.2018
 */
public class WeakInputSortedContainer<ELEMENT> implements IInputSortedElements<ELEMENT>
{
  private final WeakHashMap<ELEMENT, Integer> content = new WeakHashMap<>();
  private int currentIndex = 0;

  /**
   * Creates an empty container.
   */
  public WeakInputSortedContainer()
  {
  }

  /**
   * Create an container with initial elements.
   *
   * @param pContent a collection of elements to add initially
   */
  public WeakInputSortedContainer(Collection<ELEMENT> pContent)
  {
    pContent.forEach(this::add);
  }

  @Override
  public boolean add(ELEMENT pElement)
  {
    if (content.containsKey(pElement))
      return false;
    content.put(_requireNonNull(pElement), currentIndex++);
    return true;
  }

  @Override
  public boolean remove(ELEMENT pElement)
  {
    return content.remove(_requireNonNull(pElement)) != null;
  }

  @Override
  public void clear()
  {
    content.clear();
    currentIndex = 0;
  }

  @Override
  public int size()
  {
    return content.size();
  }

  @NotNull
  @Override
  public Iterator<ELEMENT> iterator()
  {
    return content.entrySet().stream()
        .sorted(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .iterator();
  }

  /**
   * Checks, if a element is null.
   * A runtime exception will be thrown in this case.
   *
   * @param pElement the element to check
   * @return the non null element
   */
  private ELEMENT _requireNonNull(ELEMENT pElement)
  {
    if (pElement == null)
      throw new IllegalArgumentException("Null elements are not allowed!");
    return pElement;
  }
}
