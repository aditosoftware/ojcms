package de.adito.beans.core;

import java.util.Map;

/**
 * A possibly adapted bean copy of an original bean.
 * It has its own encapsulated data core, which may consist of a reduced set of original fields.
 * The data in the core is copied. Of course, the references to the fields stay the same.
 *
 * @author Simon Danner, 18.08.2017
 */
public class BeanCopy implements IBean<BeanCopy>
{
  private final IBeanEncapsulated<BeanCopy> encapsulated;
  private final IBeanFieldActivePredicate originalActiveSupplier;

  /**
   * Creates a copy of a bean.
   *
   * @param pData                   the possibly reduced data of the bean
   * @param pOriginalActiveSupplier the original active supplier for optional bean fields
   */
  public BeanCopy(Map<IField<?>, Object> pData, IBeanFieldActivePredicate pOriginalActiveSupplier)
  {
    encapsulated = EncapsulatedBuilder.createBeanEncapsulated(new Bean.DefaultEncapsulatedBuilder(pData), BeanCopy.class);
    originalActiveSupplier = pOriginalActiveSupplier;
  }

  @Override
  public IBeanEncapsulated<BeanCopy> getEncapsulated()
  {
    return encapsulated;
  }

  @Override
  public IBeanFieldActivePredicate<BeanCopy> getFieldActiveSupplier()
  {
    return originalActiveSupplier;
  }
}
