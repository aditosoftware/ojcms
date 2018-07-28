package de.adito.beans.core.references;

import de.adito.beans.core.IBean;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * A hierarchical reference structure of a bean or a bean container.
 * The structure contains all direct references to this element.
 * They are considered as parents.
 *
 * @author Simon Danner, 01.09.2017
 */
public interface IHierarchicalStructure
{
  /**
   * All direct parents (references) to this bean element.
   * A reference is described as a node consisting of the bean and the field that holds the reference.
   *
   * @return a collection of hierarchical nodes describing the references
   */
  Set<IHierarchicalNode> getDirectParents();

  /**
   * Deletes the hierarchical structure of the source bean element.
   * May be used to improve performance, when calculating complex dependencies and references,
   * so irrelevant paths must not be analyzed within the calculation.
   */
  void destroy();

  /**
   * Describes a reference node of this structure.
   * This node stands for the source of the reference to this bean element.
   * It provides the source bean and the field, which holds to reference.
   * It is also able to iterate over the source bean's parent references,
   * which may be used to build the whole structure up to the root.
   */
  interface IHierarchicalNode extends Iterable<IHierarchicalNode>
  {
    /**
     * The bean that holds the reference.
     */
    IBean<?> getBean();

    /**
     * The bean field that holds the reference.
     */
    IHierarchicalField<?> getField();

    @NotNull
    @Override
    default Iterator<IHierarchicalNode> iterator()
    {
      return getBean().getHierarchicalStructure().getDirectParents().iterator();
    }

    /**
     * A stream of the parent references of the node/reference.
     */
    default Stream<IHierarchicalNode> streamParentNodes()
    {
      return StreamSupport.stream(spliterator(), false);
    }
  }
}
