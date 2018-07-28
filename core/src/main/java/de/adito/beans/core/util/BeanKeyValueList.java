package de.adito.beans.core.util;

import de.adito.beans.core.*;
import de.adito.beans.core.listener.IBeanChangeListener;

import java.util.*;

/**
 * A list of key-value pairs which represent a bean.
 * This may be used to utilize the bean in a structure, where a list is required.
 * The list is able to react to changes at the original bean.
 *
 * @param <BEAN> the type of the bean wich is represented
 * @author Simon Danner, 08.06.2017
 */
public class BeanKeyValueList<BEAN extends IBean<BEAN>> extends ArrayList<BeanKeyValueList.KeyValue> implements IBeanChangeListener<BEAN>
{
  /**
   * Create the key value list.
   *
   * @param pBean the bean it is based on
   */
  public BeanKeyValueList(BEAN pBean)
  {
    pBean.stream()
        .map(pFieldTuple -> new KeyValue(pFieldTuple.getField(), pFieldTuple.getValue()))
        .forEach(this::add);
    pBean.listenWeak(this);
  }

  @Override
  public <TYPE> void beanChanged(BEAN pBean, IField<TYPE> pField, TYPE pOldValue)
  {
    stream()
        .filter(pKeyValue -> pKeyValue.getField() == pField)
        .findAny()
        .ifPresent(pKeyValue -> pKeyValue.value = pBean.getValue(pField));
  }

  /**
   * Combines a bean field with the associated value of the bean.
   * This object stands for a single element of this list.
   */
  public static class KeyValue
  {
    private final IField<?> keyField;
    private Object value;

    public KeyValue(IField<?> pKeyField, Object pValue)
    {
      keyField = pKeyField;
      value = pValue;
    }

    public IField<?> getField()
    {
      return keyField;
    }

    public Object getValue()
    {
      return value;
    }

    @Override
    public boolean equals(Object pO)
    {
      if (this == pO)
        return true;
      if (pO == null || getClass() != pO.getClass())
        return false;
      KeyValue keyValue = (KeyValue) pO;
      return keyField.getName().equals(keyValue.keyField.getName()) && value.equals(keyValue.value);
    }

    @Override
    public int hashCode()
    {
      int result = keyField.getName().hashCode();
      result = 31 * result + Objects.hashCode(value);
      return result;
    }
  }
}
