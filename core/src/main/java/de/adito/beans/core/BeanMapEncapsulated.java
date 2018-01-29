package de.adito.beans.core;

import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.statistics.*;
import de.adito.beans.core.util.BeanReflector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * Implementation of the bean encapsulated data core.
 *
 * @param <BEAN> the type of bean, which is the wrapper of this data core
 * @author Simon Danner, 19.01.2017
 */
class BeanMapEncapsulated<BEAN extends IBean<BEAN>> extends LinkedHashMap<IField<?>, Object> implements IBeanEncapsulated<BEAN>
{
  private final List<IField<?>> fieldOrder = new ArrayList<>();
  private Map<IField<?>, IStatisticData> statisticData;
  private final BeanBaseData<BEAN, IBeanChangeListener<BEAN>> baseData = new BeanBaseData<>();

  /**
   * Creates the bean core with a collection of empty fields.
   * The values will be null initially.
   *
   * @param pBeanType the type of the bean containing this data core
   * @param pFields   the collection of bean fields
   */
  public BeanMapEncapsulated(Class<? extends IBean> pBeanType, Collection<IField<?>> pFields)
  {
    _createStatisticData(pFields, pBeanType);
    pFields.forEach(pField -> addField(pField, getFieldCount()));
  }

  /**
   * Creates the bean core with an initial mapping of fields and associated values.
   *
   * @param pBeanType     the type of the bean containing this data core
   * @param pFieldMapping the initial data of this core as mapping of fields and values
   */
  public BeanMapEncapsulated(Class<? extends IBean> pBeanType, Map<? extends IField<?>, Object> pFieldMapping)
  {
    _createStatisticData(pFieldMapping.keySet(), pBeanType);
    pFieldMapping.forEach((pField, pValue) -> _setValueNonType(pField, pValue, true));
  }


  @Override
  public <TYPE> TYPE getValue(IField<TYPE> pField)
  {
    //noinspection unchecked
    return (TYPE) get(pField);
  }

  @Override
  public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue)
  {
    _setValueNonType(pField, pValue, false);
  }

  @Override
  public <TYPE> void addField(IField<TYPE> pField, int pIndex)
  {
    put(pField, pField.getDefaultValue());
    fieldOrder.add(pIndex, pField);
  }

  @Override
  public <TYPE> void removeField(IField<TYPE> pField)
  {
    remove(pField);
    fieldOrder.remove(pField);
  }

  @Override
  public <TYPE> int getFieldIndex(IField<TYPE> pField)
  {
    if (!fieldOrder.contains(pField))
      throw new RuntimeException("field: " + pField.getName());
    return fieldOrder.indexOf(pField);
  }

  @Override
  public int getFieldCount()
  {
    return size();
  }

  @Override
  public Map<IField<?>, IStatisticData> getStatisticData()
  {
    return Collections.unmodifiableMap(statisticData);
  }

  @Override
  public BeanBaseData<BEAN, IBeanChangeListener<BEAN>> getBeanBaseData()
  {
    return baseData;
  }

  @NotNull
  @Override
  public Iterator<Map.Entry<IField<?>, Object>> iterator()
  {
    return entrySet().iterator();
  }

  @Override
  public Stream<IField<?>> streamFields()
  {
    return keySet().stream();
  }

  /**
   * Creates the statistic data for this encapsulated core.
   * This data contains a set of entries with the value of a field for a certain timestamp.
   * It may contain multiple sets for every field annotated with {@link Statistics}.
   *
   * @param pFields   a collection of all fields of this bean
   * @param pBeanType the type of the bean
   */
  private void _createStatisticData(Collection<? extends IField<?>> pFields, Class<? extends IBean> pBeanType)
  {
    statisticData = BeanReflector.getBeanStatisticAnnotations(pBeanType).entrySet().stream()
        .collect(Collectors.toMap(pEntry -> _findField(pFields, pEntry.getKey()), pEntry ->
        {
          Statistics statistics = pEntry.getValue();
          return new StatisticData<>(statistics.intervall(), statistics.capacity(), null);
        }));
  }

  /**
   * Resolves a field by its name.
   *
   * @param pFields    a collection of fields
   * @param pFieldName the name of the field to search
   * @return the resolved field instance
   */
  private IField<?> _findField(Collection<? extends IField> pFields, String pFieldName)
  {
    return pFields.stream()
        .filter(pField -> pField.getName().equals(pFieldName))
        .findFirst().orElseThrow(() -> new RuntimeException("fieldName: " + pFieldName));
  }

  /**
   * Sets the value for a bean field.
   *
   * @param pField         the bean field
   * @param pValue         the value to set
   * @param pAllowNewField <tt>true</tt>, if new fields are allowed
   */
  private void _setValueNonType(IField<?> pField, Object pValue, boolean pAllowNewField)
  {
    if (pValue != null && !pField.getType().isAssignableFrom(pValue.getClass()))
      throw new RuntimeException("field-type: " + pField.getType().getSimpleName() + " value-type: " + pValue.getClass().getSimpleName());

    boolean existing = containsKey(pField);
    if (!pAllowNewField && !existing)
      throw new RuntimeException("field: " + pField.getName());

    put(pField, pValue);
    if (!existing)
      fieldOrder.add(pField);
  }
}
