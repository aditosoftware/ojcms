package de.adito.ojcms.beans.references;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.fields.IField;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides references that are referring to this instance itself.
 *
 * @author Simon Danner, 25.11.2018
 */
public interface IReferenceProvider
{
  /**
   * All direct references to this bean element.
   *
   * @return a set of references
   */
  Set<BeanReference> getDirectReferences();

  /**
   * All references to this bean element, even if they are created indirectly.
   * The returned references are not connected in any way, it's only a collection of all single references,
   * that may lead to this bean element directly or over n references.
   *
   * @return a collection of references leading to this bean element in some way
   */
  default Set<BeanReference> getAllReferences()
  {
    final Set<BeanReference> allNodes = getDirectReferences();
    Set<BeanReference> newNodes = allNodes;
    do
    {
      newNodes = newNodes.stream()
          .flatMap(BeanReference::streamParentReferences)
          .filter(pNode -> allNodes.stream()
              .map(BeanReference::getBean)
              .noneMatch(pInnerBean -> pInnerBean == pNode.getBean())) //Avoid cyclic references
          .collect(Collectors.toSet());
      allNodes.addAll(newNodes);
    }
    while (newNodes.size() > 0);

    return allNodes;
  }

  /**
   * All bean fields of a certain bean within the reference structure, which lead to this bean at some place.
   * The references may be direct or accomplished over many references.
   *
   * @param pBean the bean that holds the reference
   * @return a collection of bean fields referring to this bean
   */
  default Set<IField<?>> getAllReferencesByBean(IBean<?> pBean)
  {
    return getAllReferences().stream()
        .filter(pNode -> pNode.getBean() == pBean)
        .map(BeanReference::getField)
        .collect(Collectors.toSet());
  }

  /**
   * All beans within the reference structure, which lead to this bean at some moment through a certain bean field.
   * The references may be direct or accomplished over many nodes.
   *
   * @param pField the bean field that holds the reference
   * @return a collection of beans referring to this bean
   */
  default Set<IBean<?>> getAllReferencesByField(IField<?> pField)
  {
    return getAllReferences().stream()
        .filter(pNode -> pNode.getField() == pField)
        .map(BeanReference::getBean)
        .collect(Collectors.toSet());
  }
}
