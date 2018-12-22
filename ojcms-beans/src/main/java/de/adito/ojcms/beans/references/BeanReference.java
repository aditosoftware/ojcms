package de.adito.ojcms.beans.references;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.fields.IField;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.*;

/**
 * A reference to a bean or container.
 * It provides the bean and the field, which hold to reference.
 * It is also able to iterate over the referring bean's parent references, which may be used to build the whole structure up to the root.
 *
 * @author Simon Danner, 24.11.2018
 */
public final class BeanReference extends WeakReference<IBean<?>> implements Iterable<BeanReference>
{
  private final IField<?> field;

  /**
   * Creates the bean reference.
   *
   * @param pBean  the bean that holds to reference
   * @param pField the field that holds the reference
   */
  public BeanReference(IBean<?> pBean, IField<?> pField)
  {
    super(Objects.requireNonNull(pBean));
    field = pField;
  }

  /**
   * The bean that holds the reference.
   *
   * @return a bean holding the reference
   */
  public IBean<?> getBean()
  {
    return _requiresExistingReference();
  }

  /**
   * The bean field that holds the reference.
   *
   * @return a bean field holding the reference
   */
  public IField<?> getField()
  {
    _requiresExistingReference();
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
  public String toString()
  {
    _requiresExistingReference();
    return getClass().getSimpleName() + "{" +
        "bean=" + getBean() +
        ", field=" + field +
        '}';
  }

  @Override
  public boolean equals(Object pObject)
  {
    if (this == pObject) return true;
    if (pObject == null || getClass() != pObject.getClass()) return false;

    final BeanReference other = (BeanReference) pObject;
    return _requiresExistingReference() == other.getBean() && field == other.field;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(_requiresExistingReference(), field);
  }

  /**
   * The bean holding the reference if the reference hasn't been collected by GC.
   *
   * @return the bean holding the reference
   * @throws RuntimeException if the reference does not exist anymore
   */
  private IBean<?> _requiresExistingReference()
  {
    return Optional.ofNullable(get())
        .orElseThrow(() -> new RuntimeException("This bean reference is not existing anymore!"));
  }
}
