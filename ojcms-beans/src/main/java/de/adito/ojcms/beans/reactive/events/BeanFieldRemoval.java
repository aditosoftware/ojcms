package de.adito.ojcms.beans.reactive.events;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.reactive.AbstractFieldBasedChangeEvent;

/**
 * A field has been removed from a bean.
 *
 * @param <VALUE> the value type of the removed field
 * @author Simon Danner, 18.11.2018
 */
public final class BeanFieldRemoval<VALUE> extends AbstractFieldBasedChangeEvent<VALUE, BeanFieldRemoval<VALUE>>
{
  private final VALUE fieldValue;

  /**
   * Creates the field removal event.
   *
   * @param pSource     the bean the field has been removed from
   * @param pField      the removed field
   * @param pFieldValue the value of the removed field
   */
  public BeanFieldRemoval(IBean pSource, IField<VALUE> pField, VALUE pFieldValue)
  {
    super(pSource, pField);
    fieldValue = pFieldValue;
  }

  public VALUE getFieldValue()
  {
    return fieldValue;
  }
}
