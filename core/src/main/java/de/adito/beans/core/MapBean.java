package de.adito.beans.core;

import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.util.BeanUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * A special bean that represents a {@link Map}.
 * A key-value pair of the map will be presented as a bean field with the associated value.
 * This bean can additionally be treated as normal map as well.
 *
 * @param <KEY>   the map's key type
 * @param <VALUE> the map's value type
 * @author Simon Danner, 07.02.2017
 */
public class MapBean<KEY, VALUE> extends AbstractMap<KEY, VALUE> implements IModifiableBean<MapBean<KEY, VALUE>>
{
  private final IBeanEncapsulated<MapBean<KEY, VALUE>> encapsulated;
  private final Class<? extends IField<?>> fieldType; //the field type may differ from the generic type due to converters
  private final Class<VALUE> valueType;
  private final Map<KEY, IField<?>> keyFieldMapping = new HashMap<>();
  private final Map<IField<?>, KEY> fieldKeyMapping = new HashMap<>();
  private final Function<KEY, Optional<IField<?>>> fieldCache;
  private final BiConsumer<KEY, IField<?>> fieldCacheCallback;
  private _EntrySet entrySet;

  /**
   * Creates a map representation as a bean.
   *
   * @param pMap       the source from which the bean will be created
   * @param pValueType the map's value type
   */
  public MapBean(Map<KEY, VALUE> pMap, Class<VALUE> pValueType)
  {
    this(pMap, pValueType, (pKey, pField) -> {}, pHashCode -> Optional.empty());
  }

  /**
   * Creates a map representation.
   * This constructor allows you to provide a field cache.
   * The cache stores field instances and supplies them for the same hashcode.
   * A cache may be useful when map beans are compared to each other, because bean fields are mostly compared by reference.
   * It may also be a slight performance improvement.
   * If a field is not in the cache yet, it will be created by the field factory and be given to the cache via a callback.
   *
   * @param pMap                the source map from which the bean will be created
   * @param pValueType          the map's value type
   * @param pFieldCacheCallback callback for the cache to record newly created fields
   * @param pFieldCache         the field cache for keys/fields with the same key
   */
  public MapBean(Map<KEY, VALUE> pMap, Class<VALUE> pValueType, BiConsumer<KEY, IField<?>> pFieldCacheCallback,
                 Function<KEY, Optional<IField<?>>> pFieldCache)
  {
    fieldType = BeanFieldFactory.getFieldTypeFromType(pValueType);
    valueType = pValueType;
    fieldCache = pFieldCache;
    fieldCacheCallback = pFieldCacheCallback;
    BiConsumer<LinkedHashMap<IField<?>, VALUE>, Map.Entry<KEY, VALUE>> accumulator =
        (pSorted, pEntry) -> pSorted.put(_createField(pEntry.getKey()), pEntry.getValue());
    final Map<IField<?>, VALUE> fieldValueMapping = pMap.entrySet().stream()
        .collect(LinkedHashMap::new, accumulator, LinkedHashMap::putAll);
    final List<IField<?>> fields = new ArrayList<>(fieldValueMapping.keySet());
    //noinspection unchecked
    encapsulated = EncapsulatedBuilder.createBeanEncapsulated(new Bean.DefaultEncapsulatedBuilder(fields),
                                                              (Class<MapBean<KEY, VALUE>>) getClass(), fields);
    fieldValueMapping.forEach(this::setValueConverted);
  }

  /**
   * Creates an empty map bean based on an existing map bean.
   *
   * @param pExistingMapBean an existing map bean to take the fields from
   */
  public MapBean(MapBean<KEY, VALUE> pExistingMapBean)
  {
    fieldType = pExistingMapBean.fieldType;
    valueType = pExistingMapBean.valueType;
    fieldCache = pExistingMapBean.fieldCache;
    fieldCacheCallback = pExistingMapBean.fieldCacheCallback;
    final List<IField<?>> fields = pExistingMapBean.streamFields().collect(Collectors.toList());
    //noinspection unchecked
    encapsulated = EncapsulatedBuilder.createBeanEncapsulated(new Bean.DefaultEncapsulatedBuilder(fields),
                                                              (Class<MapBean<KEY, VALUE>>) getClass(), fields);
  }

  @Override
  public IBeanEncapsulated<MapBean<KEY, VALUE>> getEncapsulated()
  {
    return encapsulated;
  }

  @NotNull
  @Override
  public Set<Entry<KEY, VALUE>> entrySet()
  {
    return entrySet == null ? (entrySet = new _EntrySet()) : entrySet;
  }

  @Override
  public VALUE put(KEY pKey, VALUE pValue)
  {
    IField<?> field;
    VALUE oldValue = null;
    if (keyFieldMapping.containsKey(pKey))
    {
      field = keyFieldMapping.get(pKey);
      oldValue = getValueConverted(field, valueType);
    }
    else
    {
      field = _createField(pKey);
      addField(field);
    }
    setValueConverted(field, pValue);
    return oldValue;
  }

  /**
   * The bean field from a map key.
   *
   * @param pKey the key to find the bean field from
   * @return the bean field for a certain key
   * @throws IllegalArgumentException, if the key is not contained
   */
  @NotNull
  public IField<?> getFieldFromKey(KEY pKey)
  {
    if (!keyFieldMapping.containsKey(pKey))
      throw new IllegalArgumentException("The key '" + pKey + "' is not existing in the map bean!");
    return keyFieldMapping.get(pKey);
  }

  /**
   * Creates a bean field for an associated map entry.
   * Fields may come from a given cache.
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
                     IField<?> field = BeanFieldFactory.createField((Class<? extends IField>) fieldType, Objects.toString(pKey), Collections.emptySet());
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

    MapBean other = (MapBean) pObject;
    //MapBeans are the same, if all fields and associated values are equal
    //noinspection unchecked
    return streamFields().allMatch(other::hasField) && !BeanUtil.compareBeanValues(this, other, streamFields()).isPresent();
  }

  @Override
  public int hashCode()
  {
    final FieldTuple[] tuples = stream().toArray(FieldTuple[]::new);
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
      return new _EntryIterator();
    }

    @Override
    public int size()
    {
      return getFieldCount();
    }
  }

  /**
   * The iterator for the entry set.
   */
  private class _EntryIterator implements Iterator<Map.Entry<KEY, VALUE>>
  {
    private final Iterator<FieldTuple<?>> tupleIterator = stream().collect(Collectors.toList()).iterator();
    private FieldTuple current;

    @Override
    public boolean hasNext()
    {
      return tupleIterator.hasNext();
    }

    @Override
    public Entry<KEY, VALUE> next()
    {
      current = tupleIterator.next();
      //noinspection unchecked
      return new AbstractMap.SimpleEntry<>(fieldKeyMapping.get(current.getField()), (VALUE) getValueConverted(current.getField(), valueType));
    }

    @Override
    public void remove()
    {
      if (current == null)
        throw new IllegalStateException();
      //noinspection unchecked
      removeField(current.getField());
      current = null;
    }
  }
}
