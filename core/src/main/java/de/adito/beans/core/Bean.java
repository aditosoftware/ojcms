package de.adito.beans.core;

import de.adito.beans.core.util.BeanReflector;

import java.util.concurrent.Executors;

/**
 * The default concrete class of the bean interface.
 * It holds the encapsulated data core, which is the only state of the bean.
 *
 * This class should be extended by any bean type of the application.
 * It may also be extended by another base class, if more base data is necessary.
 *
 * It also provides the possibility the read and change private data.
 * This can be used to enable the typical behaviour of any Java POJO.
 *
 * A specific bean of the application defines its fields as static to allow access without reflection.
 * Here is an example:
 * "public class SomeBean extends Bean {
 * public static final TextField someField = BeanFieldFactory.create(SomeBean.class)"
 * }"
 *
 * It's important to use the static field factory to create the fields.
 * So all initial data is automatically stored in the field instance.
 *
 * @param <BEAN> the specific type of this bean, especially if it is used as base class
 * @author Simon Danner, 23.08.2016
 * @see BeanFieldFactory
 */
public class Bean<BEAN extends IBean<BEAN>> implements IBean<BEAN>
{
  private final IBeanEncapsulated<BEAN> encapsulated;

  public Bean()
  {
    encapsulated = new BeanMapEncapsulated<>(getClass(), BeanReflector.getBeanMetadata(getClass()));
  }

  @Override
  public IBeanEncapsulated<BEAN> getEncapsulated()
  {
    return encapsulated;
  }

  /**
   * Returns the value of a private bean field.
   *
   * @param pField the field to which the value should be returned
   * @param <TYPE> the data type of the field
   * @return the field's value
   */
  protected <TYPE> TYPE getPrivateValue(IField<TYPE> pField)
  {
    return encapsulated.getValue(pField);
  }

  /**
   * Sets the value of a private bean field.
   *
   * @param pField the field to which the value should be set
   * @param pValue the new value
   * @param <TYPE> the data type of the field
   */
  protected <TYPE> void setPrivateValue(IField<TYPE> pField, TYPE pValue)
  {
    encapsulated.setValue(pField, pValue);
  }
}
