package de.adito.beans.core.references;

import de.adito.beans.core.IBean;

import java.util.*;

/**
 * Describes an interface to add, remove and get references of a bean element.
 * A 'referable' could be considered as the target of a reference within a bean structure.
 *
 * @author Simon Danner, 29.08.2017
 */
public interface IReferable
{
  /**
   * Returns a weak map which contains all references to this bean element.
   * The key stands for the bean, which is the source of the reference.
   * The value is a collection of bean fields that hold the direct references.
   *
   * @return a map that describes all references to this bean element
   */
  Map<IBean<?>, Set<IHierarchicalField<?>>> getWeakReferenceMap();

  /**
   * Adds a reference to this bean element.
   *
   * @param pBean  the source bean of the reference
   * @param pField the bean field that holds the reference
   */
  default void addWeakReference(IBean<?> pBean, IHierarchicalField<?> pField)
  {
    assert getWeakReferenceMap() != null;
    getWeakReferenceMap().computeIfAbsent(pBean, pKey -> new HashSet<>()).add(pField);
  }

  /**
   * Removes a reference from this bean element.
   *
   * @param pBean  the source bean of the reference
   * @param pField the bean field that held the reference
   */
  default void removeReference(IBean<?> pBean, IHierarchicalField<?> pField)
  {
    Map<IBean<?>, Set<IHierarchicalField<?>>> references = getWeakReferenceMap();
    assert references != null;
    if (!references.containsKey(pBean))
      return;
    Set<IHierarchicalField<?>> nodes = references.get(pBean);
    nodes.remove(pField);
    if (nodes.isEmpty())
      references.remove(pBean);
  }
}
