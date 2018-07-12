package de.adito.beans.core.fields;

import de.adito.beans.core.*;
import de.adito.beans.core.util.beancopy.CustomFieldCopy;
import org.jetbrains.annotations.*;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * A bean field that holds a map.
 * The original map will be transformed into a bean, which represents the mapping by its fields and according values.
 *
 * @param <TYPE> the value-type of the map.
 * @author Simon Danner, 01.02.2017
 */
public class MapField<TYPE> extends AbstractField<MapBean<TYPE>>
{
  private final Set<IField<TYPE>> fieldCache = new HashSet<>();

  public MapField(@NotNull Class<MapBean<TYPE>> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(pType, pName, pAnnotations);
  }

  /**
   * Creates a bean from a map that contains strings as keys and any objects as values.
   * The key defines the bean field's name.
   *
   * @param pMap       the map that will be transformed
   * @param pValueType the value type of the map
   * @return a (modifiable) bean, which represents the original map
   */
  public MapBean<TYPE> createBeanFromMap(Map<String, TYPE> pMap, Class<TYPE> pValueType)
  {
    return createBeanFromMap(pMap, pValueType, null);
  }

  /**
   * Creates a bean from a map that contains strings as keys and any objects as values.
   * The key defines the bean field's name.
   * This method is also able to define a field predicate, which excludes certain bean fields / map values.
   *
   * @param pMap            the map that will be transformed
   * @param pValueType      the value type of the map
   * @param pFieldPredicate an optional field predicate, which determines what fields should be in the map bean
   * @return a (modifiable) bean, which represents the original map
   */
  public MapBean<TYPE> createBeanFromMap(Map<String, TYPE> pMap, Class<TYPE> pValueType, @Nullable Predicate<IField<?>> pFieldPredicate)
  {
    MapBean<TYPE> bean = new MapBean<>(pMap, pValueType, fieldCache::add, (pFieldType, pName) ->
        fieldCache.stream()
            .filter(pField -> pField.getClass() == pFieldType && pField.getName().equals(pName))
            .findAny());
    if (pFieldPredicate != null)
      bean.removeFieldIf(pFieldPredicate);
    return bean;
  }

  /**
   * Transforms the bean back to the original map.
   *
   * @param pBean      the bean that this field belongs to
   * @param pValueType the value type of the field/map
   * @return the original map, which was represented by the bean (new instance of the map)
   */
  public Map<String, TYPE> createMapFromBean(IBean<?> pBean, Class<TYPE> pValueType)
  {
    MapBean<TYPE> mapBean = pBean.getValue(this);
    return mapBean.streamFields()
        .collect(LinkedHashMap::new,
                 (pMap, pField) -> pMap.put(pField.getName(), mapBean.getValueConverted(pField, pValueType)),
                 LinkedHashMap::putAll);
  }

  @Override
  public MapBean<TYPE> copyValue(MapBean<TYPE> pValue, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    Function<MapBean<TYPE>, MapBean<TYPE>> creator = pMapBean -> new MapBean<>(pValue.streamFields().collect(Collectors.toList()),
                                                                               pValue.getValueType());
    return pValue.createCopy(true, creator, pCustomFieldCopies);
  }
}
