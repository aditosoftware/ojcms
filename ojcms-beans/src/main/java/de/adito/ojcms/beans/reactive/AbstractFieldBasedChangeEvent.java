package de.adito.ojcms.beans.reactive;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.literals.fields.IField;

/**
 * An abstract change event that is based on a bean and an associated field.
 *
 * @param <VALUE> the value type of the field that is associated with the field
 * @param <EVENT> the actual runtime type of this event
 * @author Simon Danner, 18.11.2018
 */
public abstract class AbstractFieldBasedChangeEvent<VALUE, EVENT extends AbstractFieldBasedChangeEvent<VALUE, EVENT>>
    extends AbstractChangeEvent<IBean, EVENT>
{
  private final IField<? extends VALUE> field;

  /**
   * Creates field based change event.
   *
   * @param pSource the bean that is the source which triggered this event
   * @param pField  the field associated with the change
   */
  protected AbstractFieldBasedChangeEvent(IBean pSource, IField<? extends VALUE> pField)
  {
    super(pSource);
    field = pField;
  }

  /**
   * The field associated with the change event.
   *
   * @return the associated bean field
   */
  public IField<? extends VALUE> getField()
  {
    return field;
  }
}
