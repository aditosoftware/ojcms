package de.adito.beans.core.util;

import de.adito.beans.core.IBean;
import de.adito.beans.core.IField;
import de.adito.beans.core.listener.IBeanChangeListener;

import java.util.*;

/**
 * Eine Liste von Key-Value-Paaren, welche eine einzelne Bean darstellt.
 * Dadurch kann die Bean in KeyValue-Form an beliebiger Stelle als Liste eingesetzt werden.
 *
 * @param <BEAN> der konkrete Typ der Bean, welche hier abgebildet wird
 * @author s.danner, 08.06.2017
 */
public class BeanKeyValueList<BEAN extends IBean<BEAN>> extends ArrayList<BeanKeyValueList.KeyValue> implements IBeanChangeListener<BEAN>
{
  public BeanKeyValueList(BEAN pBean)
  {
    pBean.stream()
        .map(pEntry -> new KeyValue(pEntry.getKey(), pEntry.getValue()))
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
   * Kapselt einen Key (hier Feldname einer Bean) und den zugehörigen Wert (Datenwert des Bean-Feldes)
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
