package de.adito.beans.core.references;

import de.adito.beans.core.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * An extension of {@link IHierarchicalStructure} for beans.
 * Its main purpose is to analyze deep references within a bean hierarchical structure.
 *
 * @author Simon Danner, 29.08.2017
 */
public interface IHierarchicalBeanStructure extends IHierarchicalStructure
{
  /**
   * All bean fields of a certain bean within the parent reference structure, which lead to this bean at some place.
   * The references may be direct or accomplished over many nodes.
   *
   * @param pBean the bean that holds the reference
   * @return a collection of bean fields referring to this bean
   */
  default Set<IHierarchicalField<?>> getParentReferencesByBean(IBean<?> pBean)
  {
    return getAllParentReferences().stream()
        .filter(pNode -> pNode.getBean() == pBean)
        .map(IHierarchicalNode::getField)
        .collect(Collectors.toSet());
  }

  /**
   * All beans within the parent reference structure, which lead to this bean at some moment through a certain bean field.
   * The references may be direct or accomplished over many nodes.
   *
   * @param pField the bean field that holds the reference
   * @return a collection of beans referring to this bean
   */
  default Set<IBean<?>> getParentReferenceByField(IField<?> pField)
  {
    return getAllParentReferences().stream()
        .filter(pNode -> pNode.getField() == pField)
        .map(IHierarchicalNode::getBean)
        .collect(Collectors.toSet());
  }

  /**
   * All parent references of this bean.
   * The nodes are not connected in any way, it's only a collection of all single elements of the parent reference structure.
   * A reference may lead to this bean directly or over n nodes.
   *
   * @return a collection of reference nodes
   */
  default Set<IHierarchicalNode> getAllParentReferences()
  {
    Set<IHierarchicalNode> allNodes = getDirectParents();
    Set<IHierarchicalNode> newNodes = allNodes;
    do
    {
      newNodes = newNodes.stream()
          .flatMap(IHierarchicalNode::streamParentNodes)
          .filter(pNode -> allNodes.stream()
              .map(IHierarchicalNode::getBean)
              .noneMatch(pInnerBean -> pInnerBean == pNode.getBean())) //Avoid cyclic references
          .collect(Collectors.toSet());
      allNodes.addAll(newNodes);
    }
    while (newNodes.size() > 0);

    return allNodes;
  }
}
