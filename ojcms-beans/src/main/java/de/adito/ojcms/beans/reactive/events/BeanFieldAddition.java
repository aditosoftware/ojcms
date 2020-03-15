package de.adito.ojcms.beans.reactive.events;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.reactive.AbstractFieldBasedChangeEvent;

/**
 * A field has been added to a bean.
 *
 * @param <VALUE> the value type of the added field
 * @author Simon Danner, 18.11.2018
 */
public final class BeanFieldAddition<VALUE> extends AbstractFieldBasedChangeEvent<VALUE, BeanFieldAddition<VALUE>>
{
  /**
   * Creates the field addition event.
   *
   * @param pSource the bean the field has been added to
   * @param pField  the added field
   */
  public BeanFieldAddition(IBean pSource, IField<VALUE> pField)
  {
    super(pSource, pField);
  }
}
