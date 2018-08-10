package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Detail;
import de.adito.beans.core.util.beancopy.*;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;

/**
 * A bean field that holds a {@link Map}.
 * The original map will be transformed into a bean, which represents the mapping by its fields and associated values.
 *
 * @param <KEY>   the key type of the map
 * @param <VALUE> the value type of the map
 * @author Simon Danner, 01.02.2017
 */
public class MapField<KEY, VALUE> extends AbstractField<MapBean<KEY, VALUE>>
{
  private final Map<KEY, IField<?>> fieldCache = new HashMap<>();

  public MapField(@NotNull Class<MapBean<KEY, VALUE>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  /**
   * Creates a bean from a map.
   * The key's hashcode defines the bean field's name.
   * Each entry in the map will result in one bean field with the associated value.
   *
   * @param pMap       the map that will be transformed
   * @param pValueType the value type of the map
   * @return a (modifiable) bean, that represents the original map
   */
  public MapBean<KEY, VALUE> createBeanFromMap(Map<KEY, VALUE> pMap, Class<VALUE> pValueType)
  {
    return createBeanFromMap(pMap, pValueType, null);
  }

  /**
   * Creates a bean from a map.
   * Each entry in the map will result in one bean field with the associated value.
   * This method is also able to define a field predicate, which excludes certain bean fields / map values.
   *
   * @param pMap       the map that will be transformed
   * @param pValueType the value type of the map
   * @param pExclude   an optional field value predicate, which determines what fields should be excluded
   * @return a (modifiable) bean, that represents the original map
   */
  public MapBean<KEY, VALUE> createBeanFromMap(Map<KEY, VALUE> pMap, Class<VALUE> pValueType, @Nullable Predicate<FieldTuple<VALUE>> pExclude)
  {
    MapBean<KEY, VALUE> bean = new MapBean<>(pMap, pValueType, fieldCache::put, pKey -> Optional.ofNullable(fieldCache.get(pKey)),
                                             hasAnnotation(Detail.class));
    if (pExclude != null)
      //noinspection unchecked
      bean.removeFieldIf(pField -> pExclude.test((FieldTuple<VALUE>) pField.newUntypedTuple(bean.getValueConverted(pField, pValueType))));
    return bean;
  }

  /**
   * Transforms the bean back to a normal map.
   * The map will be a {@link LinkedHashMap}.
   *
   * @param pBean the bean that this field belongs to
   * @return the original map, which was represented by the bean (new instance of the map)
   */
  public Map<KEY, VALUE> createMapFromBean(IBean<?> pBean)
  {
    return new LinkedHashMap<>(pBean.getValue(this));
  }

  @Override
  public MapBean<KEY, VALUE> copyValue(MapBean<KEY, VALUE> pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pValue.createCopy(pMode, MapBean::new, pCustomFieldCopies);
  }
}
