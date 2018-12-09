package de.adito.ojcms.beans.references;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.fields.IField;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * A reference to a bean or container.
 * It provides the bean and the field, which hold to reference.
 * It is also able to iterate over the referring bean's parent references, which may be used to build the whole structure up to the root.
 *
 * @author Simon Danner, 24.11.2018
 */
public final class BeanReference implements Iterable<BeanReference>
{
  private final IBean<?> bean;
  private final IField<?> field;

  /**
   * Creates the bean reference.
   *
   * @param pBean  the bean that holds to reference
   * @param pField the field that holds the reference
   */
  public BeanReference(IBean<?> pBean, IField<?> pField)
  {
    bean = pBean;
    field = pField;
  }

  /**
   * The bean that holds the reference.
   *
   * @return a bean holding the reference
   */
  public IBean<?> getBean()
  {
    return bean;
  }

  /**
   * The bean field that holds the reference.
   *
   * @return a bean field holding the reference
   */
  public IField<?> getField()
  {
    return field;
  }

  @NotNull
  @Override
  public Iterator<BeanReference> iterator()
  {
    return getBean().getDirectReferences().iterator();
  }

  /**
   * A stream of the parent references of this reference.
   *
   * @return a stream of parent references
   */
  public Stream<BeanReference> streamParentReferences()
  {
    return StreamSupport.stream(spliterator(), false);
  }

  @Override
  public boolean equals(Object pObject)
  {
    if (this == pObject) return true;
    if (pObject == null || getClass() != pObject.getClass()) return false;

    BeanReference other = (BeanReference) pObject;
    return bean == other.bean && field == other.field;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(bean, field);
  }
}
