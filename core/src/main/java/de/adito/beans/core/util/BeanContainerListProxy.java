package de.adito.beans.core.util;

import de.adito.beans.core.*;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;

/**
 * A list proxy for a bean container to treat it as a List.
 *
 * @param <BEAN> the type of the beans in the container
 * @author Simon Danner, 08.02.2017
 */
public class BeanContainerListProxy<BEAN extends IBean<BEAN>> implements List<BEAN>
{
  private final IBeanContainer<BEAN> container;

  /**
   * Creates the list proxy based on an original bean container.
   *
   * @param pContainer the original bean container
   */
  public BeanContainerListProxy(IBeanContainer<BEAN> pContainer)
  {
    container = pContainer;
  }

  @Override
  public int size()
  {
    return container.size();
  }

  @Override
  public boolean isEmpty()
  {
    return size() == 0;
  }

  @Override
  public boolean contains(Object pObject)
  {
    //noinspection unchecked
    return _checkType(pObject) && container.contains((BEAN) pObject);
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> pCollection)
  {
    return pCollection.stream().allMatch(this::contains);
  }

  @NotNull
  @Override
  public Iterator<BEAN> iterator()
  {
    return new _ProxyIterator();
  }

  @NotNull
  @Override
  public BEAN[] toArray()
  {
    //noinspection unchecked
    return container.stream().toArray(pSize -> (BEAN[]) Array.newInstance(container.getBeanType(), pSize));
  }

  @NotNull
  @Override
  @SuppressWarnings("unchecked")
  public <T> T[] toArray(@NotNull T[] pArray)
  {
    int size = container.size();
    T[] array = pArray.length >= size ? pArray : (T[]) Array.newInstance(pArray.getClass().getComponentType(), size);
    container.forEachBean(pBean -> array[container.indexOf(pBean)] = (T) pBean);
    return array;
  }

  @Override
  public boolean add(@Flow(targetIsContainer = true) BEAN pBean)
  {
    container.addBean(pBean);
    return true;
  }

  @Override
  public boolean remove(Object pObject)
  {
    //noinspection unchecked
    return _checkType(pObject) && container.removeBean((BEAN) pObject);
  }

  @Override
  public boolean addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Collection<? extends BEAN> pCollection)
  {
    pCollection.forEach(this::add);
    return true;
  }

  @Override
  public boolean addAll(int pIndex, @NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Collection<? extends BEAN> pCollection)
  {
    for (BEAN bean : pCollection)
      add(pIndex++, bean);
    return true;
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> pCollection)
  {
    return pCollection.stream().allMatch(this::remove);
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> pCollection)
  {
    return container.removeBeanIf(pBean -> !pCollection.contains(pBean));
  }

  @Override
  public void clear()
  {
    container.clear();
  }

  @Override
  public BEAN get(int pIndex)
  {
    return container.getBean(pIndex);
  }

  @Override
  public BEAN set(int pIndex, @Flow(targetIsContainer = true) BEAN pElement)
  {
    return container.replaceBean(pElement, pIndex);
  }

  @Override
  public void add(int pIndex, @Flow(targetIsContainer = true) BEAN pElement)
  {
    container.addBean(pElement, pIndex);
  }

  @Override
  public BEAN remove(int pIndex)
  {
    return container.removeBean(pIndex);
  }

  @Override
  public int indexOf(Object pObject)
  {
    if (!_checkType(pObject))
      throw new ClassCastException("the type of the given object (" + pObject.getClass().getName() +
                                       ") is not compatible to the container's bean types.");
    //noinspection unchecked
    return container.indexOf((BEAN) pObject);
  }

  @Override
  public int lastIndexOf(Object pObject)
  {
    if (!_checkType(pObject))
      throw new ClassCastException("the type of the given object (" + pObject.getClass().getName() +
                                       ") is not compatible to the container's bean types.");

    final int lastIndex = size() - 1;
    return IntStream.rangeClosed(0, lastIndex)
        .map(pIndex -> lastIndex - pIndex)
        .filter(pIndex -> Objects.equals(pObject, get(pIndex)))
        .findFirst()
        .orElse(-1);
  }

  @NotNull
  @Override
  public ListIterator<BEAN> listIterator()
  {
    return new _ProxyListIterator(0);
  }

  @NotNull
  @Override
  public ListIterator<BEAN> listIterator(int pIndex)
  {
    return new _ProxyListIterator(pIndex);
  }

  @NotNull
  @Override
  public List<BEAN> subList(int pFromIndex, int pToIndex)
  {
    throw new UnsupportedOperationException("not available for bean containers");
  }

  /**
   * Checks, if any object is assignable from the container's bean type.
   *
   * @param pObject the object to check
   * @return <tt>true</tt>, if the bean type is assignable
   */
  private boolean _checkType(Object pObject)
  {
    return container.getBeanType().isAssignableFrom(pObject.getClass());
  }

  /**
   * A simple iterator implementation.
   */
  private class _ProxyIterator implements Iterator<BEAN>
  {
    protected int cursor = 0;
    protected int initialSize = container.size();

    @Override
    public boolean hasNext()
    {
      return cursor < container.size();
    }

    @Override
    public BEAN next()
    {
      checkConcurrent();
      if (hasNext())
        return container.getBean(cursor++);
      throw new NoSuchElementException();
    }

    @Override
    public void remove()
    {
      if (cursor < 1)
        throw new IllegalStateException();
      checkConcurrent();
      container.removeBean(container.getBean(--cursor));
      initialSize--;
    }

    /**
     * Checks for a concurrent modification.
     */
    protected void checkConcurrent()
    {
      if (initialSize != container.size())
        throw new ConcurrentModificationException();
    }
  }

  /**
   * Extends the simple Iterator to a ListIterator.
   */
  private class _ProxyListIterator extends _ProxyIterator implements ListIterator<BEAN>
  {
    private final int startIndex;

    public _ProxyListIterator(int pStartIndex)
    {
      startIndex = pStartIndex;
      cursor = pStartIndex;
    }

    @Override
    public boolean hasPrevious()
    {
      return cursor > startIndex;
    }

    @Override
    public BEAN previous()
    {
      checkConcurrent();
      if (hasPrevious())
        return container.getBean(--cursor);
      throw new NoSuchElementException();
    }

    @Override
    public int nextIndex()
    {
      return cursor;
    }

    @Override
    public int previousIndex()
    {
      return cursor - 1;
    }

    @Override
    public void set(BEAN pBean)
    {
      checkConcurrent();
      if (cursor > 0)
        BeanContainerListProxy.this.set(cursor - 1, pBean);
    }

    @Override
    public void add(BEAN pBean)
    {
      checkConcurrent();
      BeanContainerListProxy.this.add(cursor++, pBean);
      initialSize++;
    }
  }
}
