package de.adito.beans.core.reactive.events;

import de.adito.beans.core.*;
import de.adito.beans.core.reactive.AbstractFieldBasedChangeEvent;

/**
 * A field has been removed from a bean.
 *
 * @param <BEAN>  the type of the bean the field has been removed from
 * @param <VALUE> the value type of the removed field
 * @author Simon Danner, 18.11.2018
 */
public final class BeanFieldRemoval<BEAN extends IBean<BEAN>, VALUE> extends AbstractFieldBasedChangeEvent<BEAN, VALUE,
    BeanFieldRemoval<BEAN, VALUE>>
{
  private final VALUE fieldValue;

  /**
   * Creates the field removal event.
   *
   * @param pSource     the bean the field has been removed from
   * @param pField      the removed field
   * @param pFieldValue the value of the removed field
   */
  public BeanFieldRemoval(BEAN pSource, IField<VALUE> pField, VALUE pFieldValue)
  {
    super(pSource, pField);
    fieldValue = pFieldValue;
  }

  public VALUE getFieldValue()
  {
    return fieldValue;
  }
}
