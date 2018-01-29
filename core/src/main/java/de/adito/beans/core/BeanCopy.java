package de.adito.beans.core;

import java.util.Map;

/**
 * A possibly adapted bean copy of an original bean.
 * It has its own encapsulated data core, which may consist of a reduced set of original fields.
 * The data in the core is copied. Of course, the references to the fields stay the same.
 *
 * @author Simon Danner, 18.08.2017
 */
public class BeanCopy implements IBean
{
  private final IBeanEncapsulated<?> encapsulated;
  private final IBeanFieldActivePredicate originalActiveSupplier;

  /**
   * Creates a copy of a bean.
   *
   * @param pData                   the possibly reduced data of the bean
   * @param pOriginalActiveSupplier the original active supplier for optional bean fields
   */
  public BeanCopy(Map<IField<?>, Object> pData, IBeanFieldActivePredicate pOriginalActiveSupplier)
  {
    encapsulated = new BeanMapEncapsulated<>(getClass(), pData);
    originalActiveSupplier = pOriginalActiveSupplier;
  }

  @Override
  public IEncapsulated getEncapsulated()
  {
    return encapsulated;
  }

  @Override
  public IBeanFieldActivePredicate getFieldActiveSupplier()
  {
    return originalActiveSupplier;
  }
}
