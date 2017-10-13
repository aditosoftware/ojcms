package de.adito.beans.core;

import de.adito.beans.core.util.BeanUtil;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Ein spezieller Bean, welcher anhand einer Map erzeugt wird.
 * Dabei wird anhand des Value-Typs der Map der Bean-Feld-Typ bestimmt.
 *
 * @param <TYPE> der Value-Typ der Map
 * @author s.danner, 07.02.2017
 */
public class MapBean<TYPE> implements IModifiableBean<MapBean<TYPE>>
{
  private final IBeanEncapsulated<MapBean<TYPE>> encapsulated;
  private Function<Object, TYPE> valueConverter = null;

  /**
   * Erzeugt eine neue Map-Bean.
   *
   * @param pMap       die Map, aus welcher der Bean erzeugt werden soll
   * @param pValueType der Value-Typ der Map
   */
  public MapBean(Map<String, Object> pMap, Class<TYPE> pValueType)
  {
    this(pMap, pValueType, pField -> {}, (pFieldType, pFieldName) -> Optional.empty());
  }

  /**
   * Erzeugt eine neue Map-Bean.
   * Hier werden die Bean-Felder, welche für die Map verwendet werden, aus einem Cache genommen,
   * um eine Identifikation anhand der Key-Felder der Map-Bean zu gewährleisten.
   * Dies nützlich, wenn eine Bean, welche eine Map-Feld besitzt, neu initialisiert wird (mit Werten)
   * und anschließend überprüft werden soll, ob sich Werte innerhalb der Map geändert haben.
   * Ein Bean-Feld aus dem Cache wird dabei anhand des Datentypen und des Namen identifziert.
   * Wenn ein nicht im Cache existiert, wird es neu erzeugt und an das Cache-Callback weitergegeben,
   * um es womöglich im Cache zu registrieren.
   *
   * @param pMap                die Map, aus welcher der Bean erzeugt werden soll
   * @param pValueType          der Value-Typ der Map
   * @param pFieldCacheCallback Callback für das Felder-Caching
   * @param pFieldCache         ein Cache für Felder gleichen Typs und Names
   *                            (als BiConsumer mit eben genannten Kriterien als Parameter und dem Feld als optionalen Rückkgabewert)
   */
  public MapBean(Map<String, Object> pMap, Class<TYPE> pValueType, Consumer<IField<TYPE>> pFieldCacheCallback,
                 BiFunction<Class<? extends IField<TYPE>>, String, Optional<IField<TYPE>>> pFieldCache)
  {
    final Class<? extends IField<TYPE>> fieldType = BeanFieldFactory.getFieldTypeFromType(pValueType);
    Map<IField<TYPE>, Object> map = pMap.entrySet().stream()
        .collect(LinkedHashMap::new,
                 (pSorted, pEntry) -> {
                   IField<TYPE> field = _createField(fieldType, pEntry.getKey(), pFieldCacheCallback, pFieldCache);
                   if (valueConverter == null) //Muss nur einmal evaluiert werden -> dann immer gleich
                     valueConverter = _getValueConverter(pEntry.getValue(), field);
                   pSorted.put(field, valueConverter != null ? valueConverter.apply(pEntry.getValue()) : null);
                 },
                 LinkedHashMap::putAll);
    encapsulated = new BeanMapEncapsulated<>(getClass(), map);
  }

  @Override
  public IBeanEncapsulated<MapBean<TYPE>> getEncapsulated()
  {
    return encapsulated;
  }

  /**
   * Erzeugt ein Bean-Feld anhand eines herkömmlichen Daten-Typen
   *
   * @param pFieldType          der Typ des Feldes
   * @param pName               der Name des Feldes
   * @param pFieldCacheCallback Callback für das Felder-Caching
   * @param pFieldCache         ein Cache für Felder gleichen Typs und Names
   *                            (als BiConsumer mit eben genannten Kriterien als Parameter und dem Feld als optionalen Rückkgabewert)
   * @return ein Bean-Feld
   */
  private IField<TYPE> _createField(Class<? extends IField<TYPE>> pFieldType, String pName, Consumer<IField<TYPE>> pFieldCacheCallback,
                                    BiFunction<Class<? extends IField<TYPE>>, String, Optional<IField<TYPE>>> pFieldCache)
  {
    return pFieldCache.apply(pFieldType, pName)
        .orElseGet(() ->
                   {
                     IField<TYPE> newField = BeanFieldFactory.createField(pFieldType, pName, Collections.emptySet());
                     pFieldCacheCallback.accept(newField);
                     return newField;
                   });
  }

  /**
   * Liefert einen Konverter für den Value des Map-Eintrages.
   * Wenn dabei der Typ des Wertes nicht zum Feld-Datentypen passt, wird versucht, eine Konvertierung durchzuführen.
   * Der Konverter wird dabei vom Feld bezogen.
   *
   * @param pSourceValue ein Quell-Wert zur Ermittlung des Quell-Typen
   * @param pField       das Feld, welches Konverter bereitstellt
   * @param <SOURCE>     der generische Typ des Quell-Wertes
   * @return ein Konverter als Function, welcher ein Quell-Wert übergeben wird und den konvertierten Wert zurückgibt
   */
  @SuppressWarnings("unchecked")
  private <SOURCE> Function<SOURCE, TYPE> _getValueConverter(SOURCE pSourceValue, IField<TYPE> pField)
  {
    if (pSourceValue == null)
      return null;

    Class<SOURCE> sourceType = (Class<SOURCE>) pSourceValue.getClass();
    return pField.getType().isAssignableFrom(sourceType) ? pSource -> (TYPE) pSource :
        pField.getToConverter(sourceType)
            .orElseThrow(() -> new RuntimeException("type: " + sourceType.getSimpleName()));
  }

  @Override
  public boolean equals(Object pObject)
  {
    if (this == pObject)
      return true;

    if (pObject == null || getClass() != pObject.getClass())
      return false;

    MapBean other = (MapBean) pObject;
    //Eine Map-Bean ist dann gleich, wenn alle Felder samt ihren Datenwerten übereinstimmen
    return streamFields().allMatch(other::hasField) &&
        !BeanUtil.compareBeanValues(this, other, streamFields().collect(Collectors.toList())).isPresent();
  }

  @Override
  public int hashCode()
  {
    return stream()
        .mapToInt(pEntry -> pEntry.getKey().hashCode() * 31 + Objects.hashCode(pEntry.getValue()))
        .sum();
  }
}
