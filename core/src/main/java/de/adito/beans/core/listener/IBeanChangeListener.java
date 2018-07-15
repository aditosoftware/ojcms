package de.adito.beans.core.listener;

import de.adito.beans.core.*;

/**
 * Defines a listener for changes at a single bean.
 * A change may be a value update or an addition/removal of a field.
 * All methods of this interface are defined as default methods to enable an adapter behaviour.
 *
 * @param <BEAN> the type of the bean for which this listener should be registered
 * @author Simon Danner, 23.08.2016
 */
public interface IBeanChangeListener<BEAN extends IBean<BEAN>>
{
  /**
   * The value of a bean field has been changed.
   *
   * @param pBean     the bean that the changed field belongs to
   * @param pField    the changed field
   * @param pOldValue the previous value of this field
   * @param <TYPE>    the inner data type of the changed field
   */
  default <TYPE> void beanChanged(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
  {
  }

  /**
   * A field has been added to the bean.
   *
   * @param pBean  the bean to which the new field has been added
   * @param pField the new field
   * @param <TYPE> the inner data type of the new field
   */
  default <TYPE> void fieldAdded(BEAN pBean, IField<TYPE> pField)
  {
  }

  /**
   * A field has been removed from the bean.
   *
   * @param pBean     the bean that the field has been removed from
   * @param pField    the removed field
   * @param pOldValue the previous value of the field before its removal
   * @param <TYPE>    the inner data type of the removed field
   */
  default <TYPE> void fieldRemoved(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
  {
  }
}
