package de.adito.beans.core.reactive.events;

import de.adito.beans.core.*;
import de.adito.beans.core.reactive.AbstractFieldBasedChangeEvent;

/**
 * A bean's value has been changed.
 *
 * @param <BEAN>  the type of the changed bean
 * @param <VALUE> the type of the changed value
 * @author Simon Danner, 16.11.2018
 */
public final class BeanValueChange<BEAN extends IBean<BEAN>, VALUE> extends AbstractFieldBasedChangeEvent<BEAN, VALUE,
    BeanValueChange<BEAN, VALUE>>
{
  private final VALUE oldValue;
  private final VALUE newValue;

  /**
   * Creates the value change event.
   *
   * @param pSource   the bean that is the source of the value change event
   * @param pField    the field from which the value has been changed
   * @param pOldValue the old value of the field
   * @param pNewValue the new changed value
   */
  public BeanValueChange(BEAN pSource, IField<VALUE> pField, VALUE pOldValue, VALUE pNewValue)
  {
    super(pSource, pField);
    oldValue = pOldValue;
    newValue = pNewValue;
  }

  /**
   * The value of the field before the change event.
   *
   * @return the old field value
   */
  public VALUE getOldValue()
  {
    return oldValue;
  }

  /**
   * The new value of the field.
   *
   * @return the new value
   */
  public VALUE getNewValue()
  {
    return newValue;
  }
}
