package de.adito.beans.core.references;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Erweitert die hierarchische Struktur speziell für Bean-Strukturen. (Die Struktur bezieht sich auf eine Bean)
 * Hier kann vor allem eine tiefe Struktur untersucht werden (nicht nur die direkten Parents).
 * Beispielsweise könnte es interessant sein, welche Beans mit einem bestimmten Feld auf eine Bean referenzieren.
 *
 * @author s.danner, 29.08.2017
 * @see IHierarchicalStructure (für die Basis)
 */
public interface IHierarchicalBeanStructure extends IHierarchicalStructure
{
  /**
   * Liefert alle Felder einer bestimmten Bean, welche auf diese Bean referenzieren.
   *
   * @param pBean die Bean, welche die Referenz hält
   * @return eine Menge von Referenz-Feldern
   */
  default Set<IHierarchicalField<?>> getParentReferencesByBean(IBean<?> pBean)
  {
    return getAllParentReferences().stream()
        .filter(pNode -> pNode.getBean() == pBean)
        .map(IHierarchicalNode::getField)
        .collect(Collectors.toSet());
  }

  /**
   * Liefert alle Beans, welche über ein bestimmtes Feld auf diese Bean referenzieren.
   *
   * @param pField das bestimmte Feld
   * @return eine Menge von Referenz-Beans
   */
  default Set<IBean<?>> getParentReferenceByField(IField<?> pField)
  {
    return getAllParentReferences().stream()
        .filter(pNode -> pNode.getField() == pField)
        .map(IHierarchicalNode::getBean)
        .collect(Collectors.toSet());
  }

  /**
   * Liefert alle Parent-Abhängigkeiten dieser Bean.
   * Die Referenzen sind dabei nicht untereinander abhängig.
   * Sie sind lediglich irgendwie mit dieser Bean in Verbindung (direkt oder über n beliebige Knoten).
   *
   * @return eine Menge von Referenzen (aus Bean + Feld).
   */
  default Set<IHierarchicalNode> getAllParentReferences()
  {
    Set<IHierarchicalNode> allNodes = getDirectParents();
    Set<IHierarchicalNode> newNodes;
    do
    {
      newNodes = allNodes.stream()
          .flatMap(IHierarchicalNode::streamParentNodes)
          .filter(pNode -> allNodes.stream()
              .map(IHierarchicalNode::getBean)
              .noneMatch(pInnerBean -> pInnerBean == pNode.getBean()))
          .collect(Collectors.toSet());
      //Alle hinzufügen
      allNodes.addAll(newNodes);
    }
    while (newNodes.size() > 0);

    return allNodes;
  }
}
