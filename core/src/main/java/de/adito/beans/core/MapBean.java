package de.adito.beans.core;

import de.adito.beans.core.util.BeanUtil;

import java.util.*;
import java.util.function.*;

/**
 * A special bean that represents a {@link Map}.
 * A key-value pair of the map will be presented as a bean field with the associated value.
 * This bean is based on a generic data type which is the value type of the map and will be used as data type for the bean fields.
 *
 * @param <TYPE> the Map's value type
 * @author Simon Danner, 07.02.2017
 */
public class MapBean<TYPE> implements IModifiableBean<MapBean<TYPE>>
{
  private final IBeanEncapsulated<MapBean<TYPE>> encapsulated;
  private final Class<TYPE> valueType;
  private Function<Object, TYPE> valueConverter = null;

  /**
   * Creates a map representation as a bean
   *
   * @param pMap       the source from which the bean will be created
   * @param pValueType the map's value type
   */
  public MapBean(Map<String, TYPE> pMap, Class<TYPE> pValueType)
  {
    this(pMap, pValueType, pField -> {}, (pFieldType, pFieldName) -> Optional.empty());
  }

  /**
   * Creates a map representation.
   * This constructor allows you to provide a field cache.
   * The cache stores field instances and supplies them for same names and types.
   * A cache may be useful when MapBeans are compared to each other, because bean fields are mostly compared by reference.
   * It may also be a slight performance improvement.
   * If a field is not in the cache yet, it will be created by the field factory and given to the cache via a callback.
   *
   * @param pMap                the source from which the bean will be created
   * @param pValueType          the map's value type
   * @param pFieldCacheCallback callback for the cache to record newly created fields
   * @param pFieldCache         the field cache for beans with the same name and field type
   *                            (a BiConsumer with the identification arguments and an optional field return type)
   */
  public MapBean(Map<String, TYPE> pMap, Class<TYPE> pValueType, Consumer<IField<TYPE>> pFieldCacheCallback,
                 BiFunction<Class<? extends IField<TYPE>>, String, Optional<IField<TYPE>>> pFieldCache)
  {
    valueType = pValueType;
    final Class<? extends IField<TYPE>> fieldType = BeanFieldFactory.getFieldTypeFromType(valueType);
    Map<IField<TYPE>, Object> map = pMap.entrySet().stream()
        .collect(LinkedHashMap::new,
                 (pSorted, pEntry) -> {
                   IField<TYPE> field = _createField(fieldType, pEntry.getKey(), pFieldCacheCallback, pFieldCache);
                   if (valueConverter == null) //has to be the same for every entry
                     valueConverter = _getValueConverter(pEntry.getValue(), field);
                   pSorted.put(field, valueConverter != null ? valueConverter.apply(pEntry.getValue()) : null);
                 },
                 LinkedHashMap::putAll);
    //noinspection unchecked
    encapsulated = EncapsulatedBuilder.createBeanEncapsulated(new Bean.DefaultEncapsulatedBuilder(map), (Class<MapBean<TYPE>>) getClass(),
                                                              new ArrayList<>(map.keySet()));
  }

  /**
   * Creates an empty map bean based on a list of bean fields.
   *
   * @param pFields    a list of bean fields
   * @param pValueType the map's value type
   */
  public MapBean(List<IField<?>> pFields, Class<TYPE> pValueType)
  {
    valueType = pValueType;
    //noinspection unchecked
    encapsulated = EncapsulatedBuilder.createBeanEncapsulated(new Bean.DefaultEncapsulatedBuilder(pFields), (Class<MapBean<TYPE>>) getClass(),
                                                              new ArrayList<>(pFields));
  }

  @Override
  public IBeanEncapsulated<MapBean<TYPE>> getEncapsulated()
  {
    return encapsulated;
  }

  /**
   * The value type of this map bean.
   */
  public Class<TYPE> getValueType()
  {
    return valueType;
  }

  /**
   * Creates a bean field for an associated map entry.
   * Fields may come from a given cache.
   *
   * @param pFieldType          the field's type
   * @param pName               the field's name
   * @param pFieldCacheCallback callback for the cache to record newly created fields
   * @param pFieldCache         the field cache for beans with the same name and field type
   *                            (a BiConsumer with the identification arguments and an optional field return type)
   * @return the bean field for the map entry
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
   * A value converter for the bean fields.
   * If the field's type is not assignable from the value's type from the map, a converter will be used.
   * A bean field is able to provide certain converters (for example {@link Date} <-> {@link java.time.Instant}
   *
   * @param pSourceValue the source value from the map
   * @param pField       the field for which the value should be set
   * @param <SOURCE>     the generic type of the source value
   * @return the converter as function that returns the data type from the certain source value
   */
  @SuppressWarnings("unchecked")
  private <SOURCE> Function<SOURCE, TYPE> _getValueConverter(SOURCE pSourceValue, IField<TYPE> pField)
  {
    if (pSourceValue == null)
      return null;

    Class<SOURCE> sourceType = (Class<SOURCE>) pSourceValue.getClass();
    return pField.getType().isAssignableFrom(sourceType) ? pSource -> (TYPE) pSource :
        pField.getToConverter(sourceType)
            .orElseThrow(() -> new RuntimeException("There is no suitable converter for the map values: " + sourceType.getSimpleName()));
  }

  @Override
  public boolean equals(Object pObject)
  {
    if (this == pObject)
      return true;

    if (pObject == null || getClass() != pObject.getClass())
      return false;

    MapBean other = (MapBean) pObject;
    //MapBeans are the same, if all fields and associated values are equal
    //noinspection unchecked
    return streamFields().allMatch(other::hasField) && !BeanUtil.compareBeanValues(this, other, streamFields()).isPresent();
  }

  @Override
  public int hashCode()
  {
    return stream()
        .mapToInt(pFieldTuple -> pFieldTuple.getField().hashCode() * 31 + Objects.hashCode(pFieldTuple.getValue()) * 31)
        .sum();
  }
}
