package de.adito.beans.core.references;

import de.adito.beans.core.IField;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Defines a special bean field that holds references to other bean elements (bean or container) within a hierarchical structure.
 *
 * @param <TYPE> the inner data type of this field
 * @author Simon Danner, 29.08.2017
 */
public interface IHierarchicalField<TYPE> extends IField<TYPE>
{
  /**
   * Takes the value of this field and returns all references, which are created by this bean field.
   *
   * @param pValue the value of the field from the consisting bean
   * @return a collection of referables
   */
  Collection<IReferable> getReferables(@Nullable TYPE pValue);
}
