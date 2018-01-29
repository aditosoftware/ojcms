package de.adito.beans.core.util;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Johannes Boesl, 15.03.13
 */
public class WeakArrayList<E> implements List<E>
{
  private ArrayList<Reference<E>> l = new ArrayList<>();

  @Override
  public int size()
  {
    return _getSnapshot().size();
  }

  @Override
  public boolean isEmpty()
  {
    return size() == 0;
  }

  @Override
  public boolean contains(Object o)
  {
    return o == null ? l.contains(null) : l.contains(_toWeak(o));
  }

  @NotNull
  @Override
  public Iterator<E> iterator()
  {
    return _getSnapshot().iterator();
  }

  @NotNull
  @Override
  public Object[] toArray()
  {
    return _getSnapshot().toArray();
  }

  @NotNull
  @Override
  public <T> T[] toArray(@NotNull T[] a)
  {
    return _getSnapshot().toArray(a);
  }

  @Override
  public boolean add(E e)
  {
    return e == null ? l.add(null) : l.add(new _WeakRef<>(e));
  }

  @Override
  public boolean remove(Object o)
  {
    return o == null ? l.remove(null) : l.remove(new _WeakRef<>(o));
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c)
  {
    return _getSnapshot().containsAll(c);
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends E> c)
  {
    boolean atLeastOneChange = false;
    for (E e : c)
      atLeastOneChange |= add(e);
    return atLeastOneChange;
  }

  @Override
  public boolean addAll(int index, @NotNull Collection<? extends E> c)
  {
    boolean atLeastOneChange = false;
    for (E e : c)
    {
      add(index++, e);
      atLeastOneChange = true;
    }
    return atLeastOneChange;
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c)
  {
    boolean atLeastOneChange = false;
    for (Object o : c)
      atLeastOneChange |= remove(o);
    return atLeastOneChange;
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c)
  {
    boolean atLeastOneChange = false;
    for (E e : _getSnapshot())
    {
      if (!c.contains(e))
        atLeastOneChange |= remove(e);
    }
    return atLeastOneChange;
  }

  @Override
  public void clear()
  {
    l.clear();
  }

  @Override
  public E get(int index)
  {
    return _getSnapshot().get(index);
  }

  @Override
  public E set(int index, E element)
  {
    Reference<E> set = l.set(index, _toWeak(element));
    return set == null ? null : set.get();
  }

  @Override
  public void add(int index, E element)
  {
    l.add(index, _toWeak(element));
  }

  @Override
  public E remove(int index)
  {
    Reference<E> remove = l.remove(index);
    return remove == null ? null : remove.get();
  }

  @Override
  public int indexOf(Object o)
  {
    return _getSnapshot().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o)
  {
    return _getSnapshot().lastIndexOf(o);
  }

  @NotNull
  @Override
  public ListIterator<E> listIterator()
  {
    return _getSnapshot().listIterator();
  }

  @NotNull
  @Override
  public ListIterator<E> listIterator(int index)
  {
    return _getSnapshot().listIterator(index);
  }

  @NotNull
  @Override
  public List<E> subList(int fromIndex, int toIndex)
  {
    return _getSnapshot().subList(fromIndex, toIndex);
  }

  private List<E> _getSnapshot()
  {
    ArrayList<E> snap = new ArrayList<>();
    for (Iterator<Reference<E>> i = l.iterator(); i.hasNext(); )
    {
      Reference<E> next = i.next();
      if (next == null)
        snap.add(null);
      else
      {
        E e = next.get();
        if (e == null)
          i.remove();
        else
          snap.add(e);
      }
    }
    return snap;
  }

  private <T> Reference<T> _toWeak(T pValue)
  {
    return new _WeakRef<>(pValue);
  }

  /**
   * WeakReference-Impl
   */
  private static class _WeakRef<T> extends WeakReference<T>
  {
    private _WeakRef(T referent)
    {
      super(referent);
    }

    public boolean equals(Object other)
    {
      T t = get();
      if (other instanceof Reference)
        return t == ((Reference) other).get();
      return t == other;
    }
  }
}
