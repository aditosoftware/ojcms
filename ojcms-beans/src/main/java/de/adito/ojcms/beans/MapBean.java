package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.Detail;
import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.MapBasedBeanDataSource;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import de.adito.ojcms.utils.IndexBasedIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * A special bean that represents a {@link Map}.
 * A key-value pair of the map will be presented as a bean field with the associated value.
 *
 * @param <KEY>   the map's key type
 * @param <VALUE> the map's value type
 * @author Simon Danner, 07.02.2017
 */
@RequiresEncapsulatedAccess
final class MapBean<KEY, VALUE> extends AbstractMap<KEY, VALUE> implements IMapBean<KEY, VALUE>
{
  private final IEncapsulatedBeanData encapsulated;
  private final boolean isDetail;
  private final Class<? extends IField<?>> fieldType; //the field type may differ from the generic type due to value converters
  private final Class<VALUE> valueType;
  private final Map<KEY, IField<?>> keyFieldMapping;
  private final Map<IField<?>, KEY> fieldKeyMapping;
  private final Function<KEY, Optional<IField<?>>> fieldCache;
  private final BiConsumer<KEY, IField<?>> fieldCacheCallback;
  private final _EntrySet entrySet = new _EntrySet();

  /**
   * Creates a map representation as bean.
   * This constructor allows to provide a field cache.
   * The cache stores field instances and supplies them for the same map key.
   * A cache may be useful when map beans are compared to each other, because bean fields are mostly compared by reference
   * and wouldn't be considered as equal if the field instances are different.
   * It may also be a slight performance improvement.
   * If a field is not in the cache yet, it will be created by the field factory and be given to the cache via a callback.
   *
   * @param pMap                the source map from which the bean will be created
   * @param pValueType          the map's value type
   * @param pFieldCacheCallback callback for the cache to record newly created fields
   * @param pFieldCache         the field cache for keys/fields with the same key
   * @param pIsDetail           <tt>true</tt>, if this map bean is considered as detail
   */
  MapBean(Map<KEY, VALUE> pMap, Class<VALUE> pValueType, BiConsumer<KEY, IField<?>> pFieldCacheCallback,
          Function<KEY, Optional<IField<?>>> pFieldCache, boolean pIsDetail)
  {
    fieldType = BeanFieldFactory.getFieldTypeFromType(pValueType);
    valueType = pValueType;
    isDetail = pIsDetail;
    keyFieldMapping = new HashMap<>();
    fieldKeyMapping = new HashMap<>();
    fieldCache = pFieldCache;
    fieldCacheCallback = pFieldCacheCallback;
    final BiConsumer<LinkedHashMap<IField<?>, VALUE>, Map.Entry<KEY, VALUE>> accumulator =
        (pSorted, pEntry) -> pSorted.put(_createField(pEntry.getKey()), pEntry.getValue());
    final Map<IField<?>, VALUE> fieldValueMapping = pMap.entrySet().stream()
        .collect(LinkedHashMap::new, accumulator, Map::putAll);
    final List<IField<?>> fields = new ArrayList<>(fieldValueMapping.keySet());
    encapsulated = new EncapsulatedBeanData(new MapBasedBeanDataSource(fields), fields);
    fieldValueMapping.forEach(this::setValueConverted);
  }

  /**
   * Creates a copy of an existing map bean.
   *
   * @param pExistingMapBean the existing map bean to copy
   */
  MapBean(MapBean<KEY, VALUE> pExistingMapBean)
  {
    isDetail = pExistingMapBean.isDetail;
    fieldType = pExistingMapBean.fieldType;
    valueType = pExistingMapBean.valueType;
    keyFieldMapping = new HashMap<>(pExistingMapBean.keyFieldMapping);
    fieldKeyMapping = new HashMap<>(pExistingMapBean.fieldKeyMapping);
    fieldCache = pExistingMapBean.fieldCache;
    fieldCacheCallback = pExistingMapBean.fieldCacheCallback;
    final List<IField<?>> fields = pExistingMapBean.streamFields().collect(Collectors.toList());
    encapsulated = new EncapsulatedBeanData(new MapBasedBeanDataSource(pExistingMapBean), fields);
  }

  @Override
  public IEncapsulatedBeanData getEncapsulatedData()
  {
    return encapsulated;
  }

  @NotNull
  @Override
  public Set<Entry<KEY, VALUE>> entrySet()
  {
    return entrySet;
  }

  @Override
  public VALUE put(KEY pKey, VALUE pValue)
  {
    final IField<?> field;
    VALUE oldValue = null;
    if (keyFieldMapping.containsKey(pKey))
    {
      field = keyFieldMapping.get(pKey);
      oldValue = getValueConverted(field, valueType);
    }
    else
    {
      field = _createField(pKey);
      encapsulated.addField(field, encapsulated.getFieldCount());
    }
    setValueConverted(field, pValue);
    return oldValue;
  }

  /**
   * Creates a bean field for an associated map entry.
   * Fields may come from a given cache. The field's name depends on the toString-representation of the map key.
   *
   * @param pKey the key of the entry
   * @return the bean field for the map entry
   */
  private IField<?> _createField(KEY pKey)
  {
    final IField<?> newField = fieldCache.apply(pKey)
        .orElseGet(() ->
                   {
                     //noinspection unchecked
                     final IField<?> field = BeanFieldFactory.createField((Class<? extends IField>) fieldType, Objects.toString(pKey),
                                                                          Collections.singleton(Detail.INSTANCE));
                     fieldCacheCallback.accept(pKey, field);
                     return field;
                   });
    keyFieldMapping.put(pKey, newField);
    fieldKeyMapping.put(newField, pKey);
    return newField;
  }

  @Override
  public boolean equals(Object pObject)
  {
    if (this == pObject)
      return true;

    if (pObject == null || getClass() != pObject.getClass())
      return false;

    final MapBean other = (MapBean) pObject;
    //MapBeans are the same, if all fields and associated values are equal
    //noinspection unchecked
    return encapsulated.getFieldCount() == other.encapsulated.getFieldCount() &&
        encapsulated.streamFields().allMatch(other::hasField) && encapsulated.streamFields()
        .map(pField -> (IField) pField)
        .allMatch(pIdentifierField -> Objects.equals(getValue(pIdentifierField), other.getValue(pIdentifierField)));
  }

  @Override
  public int hashCode()
  {
    final FieldValueTuple[] tuples = encapsulated.stream().toArray(FieldValueTuple[]::new);
    return Objects.hash((Object[]) tuples);
  }

  /**
   * The entry set for the map.
   */
  private class _EntrySet extends AbstractSet<Map.Entry<KEY, VALUE>>
  {
    @NotNull
    @Override
    public Iterator<Entry<KEY, VALUE>> iterator()
    {
      return IndexBasedIterator.buildIterator(this::_createEntryForField, this::size)
          .withRemover(encapsulated::removeFieldAtIndex)
          .createIterator();
    }

    @Override
    public int size()
    {
      return encapsulated.getFieldCount();
    }

    /**
     * Creates a map entry for a certain bean field at a specific field index.
     *
     * @param pFieldIndex the field index to create the map entry for
     * @return the newly created entry
     */
    private Entry<KEY, VALUE> _createEntryForField(int pFieldIndex)
    {
      final IField<?> field = encapsulated.getFieldAtIndex(pFieldIndex);
      return new AbstractMap.SimpleEntry<>(fieldKeyMapping.get(field), getValueConverted(field, valueType));
    }
  }
}
