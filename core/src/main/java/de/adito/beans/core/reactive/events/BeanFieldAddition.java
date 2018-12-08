package de.adito.beans.core.reactive.events;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.IField;
import de.adito.beans.core.reactive.AbstractFieldBasedChangeEvent;

/**
 * A field has been added to a bean.
 *
 * @param <BEAN>  the type of the bean the field has been added to
 * @param <VALUE> the value type of the added field
 * @author Simon Danner, 18.11.2018
 */
public final class BeanFieldAddition<BEAN extends IBean<BEAN>, VALUE> extends AbstractFieldBasedChangeEvent<BEAN, VALUE,
    BeanFieldAddition<BEAN, VALUE>>
{
  /**
   * Creates the field addition event.
   *
   * @param pSource the bean the field has been added to
   * @param pField  the added field
   */
  public BeanFieldAddition(BEAN pSource, IField<VALUE> pField)
  {
    super(pSource, pField);
  }
}
