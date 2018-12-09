package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.references.IReferenceProvider;

/**
 * Holds references to itself. A 'referable' could be considered as the target of a reference within a bean structure.
 *
 * @author Simon Danner, 29.08.2017
 */
@EncapsulatedData
interface IReferable extends IReferenceProvider
{
  /**
   * Adds a reference to this bean element.
   *
   * @param pBean  the source bean of the reference
   * @param pField the bean field that holds the reference
   */
  void addWeakReference(IBean<?> pBean, IField<?> pField);

  /**
   * Removes a reference from this bean element.
   *
   * @param pBean  the source bean of the reference
   * @param pField the bean field that held the reference
   */
  void removeReference(IBean<?> pBean, IField<?> pField);
}
