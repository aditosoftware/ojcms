package de.adito.beans.core;

import de.adito.beans.core.annotations.Statistics;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.statistics.StatisticData;
import de.adito.beans.core.util.BeanReflector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * Implementierung des Bean-Daten-Kerns.
 *
 * @param <BEAN> der generische Typ der Bean, zu welcher dieser Datenkern gehört
 * @author s.danner, 19.01.2017
 */
class BeanMapEncapsulated<BEAN extends IBean<BEAN>> extends LinkedHashMap<IField<?>, Object> implements IBeanEncapsulated<BEAN>
{
  private final List<IField<?>> fieldOrder = new ArrayList<>();
  private Map<IField<?>, IStatisticData> statisticData;
  private final BeanBaseData<BEAN, IBeanChangeListener<BEAN>> baseData = new BeanBaseData<>();

  /**
   * Erzeugt den Kern anhand einer Menge von Bean-Feldern.
   *
   * @param pBeanType der Typ des Beans, zu welchem dieser Kern gehört
   * @param pFields   die Felder des Beans
   */
  public BeanMapEncapsulated(Class<? extends IBean> pBeanType, Collection<IField<?>> pFields)
  {
    _createStatisticData(pFields, pBeanType);
    pFields.forEach(pField -> addField(pField, getFieldCount()));
  }

  /**
   * Erzeugt den Kern anhand initialer Felder und Werte als Map
   *
   * @param pBeanType     der Typ des Beans, zu welchem dieser Kern gehört
   * @param pFieldMapping die initiale Map von Feldern und Werten
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
   * Erzeugt die statistischen Daten zu diesem Kern.
   * Dabei werden die Annotations der Felder ausgewertet und wenn vorhanden initialisiert.
   *
   * @param pFields   die Felder dieses Kerns
   * @param pBeanType der Typ des Beans, zu welchem dieser Kern gehört
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
   * Sucht ein Feld in einer Menge von Bean-Feldern anhand des Namen
   *
   * @param pFields    die Menge von Feldern
   * @param pFieldName der Name des Feldes
   * @return das gefundene Feld
   */
  private IField<?> _findField(Collection<? extends IField> pFields, String pFieldName)
  {
    return pFields.stream()
        .filter(pField -> pField.getName().equals(pFieldName))
        .findFirst().orElseThrow(() -> new RuntimeException("fieldName: " + pFieldName));
  }

  /**
   * Setzt den Wert für ein Bean-Feld.
   *
   * @param pField         das Bean-Feld
   * @param pValue         der Wert, welcher gesetzt werden soll
   * @param pAllowNewField sind neue Felder erlaubt?
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
