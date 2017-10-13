package de.adito.beans.core.references;

import de.adito.beans.core.IBean;

import java.util.*;

/**
 * Beschreibt eine Schnittstelle zum Hinzufügen, Entfernen und Abfragen von Referenzen auf ein Bean-Element.
 *
 * @author s.danner, 29.08.2017
 */
public interface IReferable
{
  /**
   * Liefert eine Map, welche die Referenzen auf die Bean oder den Bean-Container dieses Datenkerns beinhaltet.
   * Der Key steht dabei für die Bean, welche die Referenz hält.
   * Der Value sind eine Menge aus Feldern, welche für solch eine Referenz (Hierarchie) verantwortlich sind
   *
   * @return ein Map, mit einer Bean als Key und einer Menge von Hierarchie-Feldern als Value
   */
  Map<IBean<?>, Set<IHierarchicalField<?>>> getWeakReferenceMap();

  /**
   * Fügt eine Referenz hinzu.
   *
   * @param pBean  die Bean, welche die Referenz hält
   * @param pField das Feld, welches die Referenz hält
   */
  default void addWeakReference(IBean<?> pBean, IHierarchicalField<?> pField)
  {
    assert getWeakReferenceMap() != null;
    getWeakReferenceMap().computeIfAbsent(pBean, pKey -> new HashSet<>()).add(pField);
  }

  /**
   * Entfernt eine Referenz.
   *
   * @param pBean  die Bean, welche die Referenz hält
   * @param pField das Feld, welches die Referenz hält
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

  /**
   * Standard-Implementierung einer Hierarchie-Node.
   */
  class HierarchicalNodeImpl implements IHierarchicalBeanStructure.IHierarchicalNode
  {
    private final IBean<?> bean;
    private final IHierarchicalField<?> field;

    public HierarchicalNodeImpl(IBean<?> pBean, IHierarchicalField<?> pField)
    {
      bean = pBean;
      field = pField;
    }

    @Override
    public IBean<?> getBean()
    {
      return bean;
    }

    @Override
    public IHierarchicalField<?> getField()
    {
      return field;
    }

    @Override
    public boolean equals(Object pObject)
    {
      if (this == pObject) return true;
      if (pObject == null || getClass() != pObject.getClass()) return false;

      HierarchicalNodeImpl other = (HierarchicalNodeImpl) pObject;
      return bean == other.bean && field == other.field;
    }

    @Override
    public int hashCode()
    {
      int result = bean.hashCode();
      result = 31 * result + field.hashCode();
      return result;
    }
  }
}
