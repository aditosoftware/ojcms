package de.adito.ojcms.utils.collections;

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
    if (content.containsKey(Objects.requireNonNull(pElement)))
      return false;
    content.put(pElement, currentIndex++);
    return true;
  }

  @Override
  public boolean remove(ELEMENT pElement)
  {
    return content.remove(Objects.requireNonNull(pElement)) != null;
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
    return content.entrySet().stream() //
        .sorted(Map.Entry.comparingByValue()) //
        .map(Map.Entry::getKey) //
        .iterator();
  }
}
