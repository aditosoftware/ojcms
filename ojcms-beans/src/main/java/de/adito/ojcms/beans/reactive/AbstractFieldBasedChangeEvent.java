package de.adito.ojcms.beans.reactive;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.fields.IField;

/**
 * An abstract change event that is based on a bean and an associated field.
 *
 * @param <BEAN>  the type of the changed bean
 * @param <VALUE> the value type of the field that is associated with the field
 * @param <EVENT> the actual runtime type of this event
 * @author Simon Danner, 18.11.2018
 */
public abstract class AbstractFieldBasedChangeEvent<BEAN extends IBean<BEAN>, VALUE, EVENT extends AbstractFieldBasedChangeEvent<BEAN, VALUE, EVENT>>
    extends AbstractChangeEvent<BEAN, EVENT>
{
  private final IField<VALUE> field;

  /**
   * Creates field based change event.
   *
   * @param pSource the bean that is the source which triggered this event
   * @param pField  the field associated with the change
   */
  protected AbstractFieldBasedChangeEvent(BEAN pSource, IField<VALUE> pField)
  {
    super(pSource);
    field = pField;
  }

  /**
   * The field associated with the change event.
   *
   * @return the associated bean field
   */
  public IField<VALUE> getField()
  {
    return field;
  }
}
