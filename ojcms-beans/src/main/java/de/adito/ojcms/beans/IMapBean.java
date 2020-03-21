package de.adito.ojcms.beans;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.literals.fields.IField;

import java.util.*;
import java.util.function.*;

/**
 * Combines a {@link Map} and a {@link IBean}.
 * A key-value pair of the map will be represented as a bean field with the associated value.
 * This combination gives the possibility to use the map value of a bean field in circumstances where an abstract component expects beans.
 * This might be a graphical representation format.
 *
 * @param <KEY>   the map's key type
 * @param <VALUE> the map's value type
 * @author Simon Danner, 22.12.2018
 */
public interface IMapBean<KEY, VALUE> extends Map<KEY, VALUE>, IBean
{
  /**
   * Creates a new map bean from an existing map.
   * The cache that has to be supplied stores field instances and supplies them for the same map key.
   * A cache may be useful when map beans are compared to each other, because bean fields are mostly compared by reference
   * and wouldn't be considered as equal if the field instances are different.
   * It may also be a slight performance improvement.
   * If a field is not in the cache yet, it will be created by the field factory and be given to the cache via a callback.
   *
   * @param pMap                the source map from which the map bean will be created
   * @param pValueType          the map's value type
   * @param pFieldCacheCallback callback for the cache to record newly created fields
   * @param pFieldCache         the field cache for keys/fields with the same key
   * @param pIsDetail           <tt>true</tt>, if this map bean is considered as detail
   */
  static <KEY, VALUE> IMapBean<KEY, VALUE> createFromMap(Map<KEY, VALUE> pMap, Class<VALUE> pValueType,
                                                         BiConsumer<KEY, IField<?>> pFieldCacheCallback,
                                                         Function<KEY, Optional<IField<?>>> pFieldCache, boolean pIsDetail)
  {
    return new MapBean<>(pMap, pValueType, pFieldCacheCallback, pFieldCache, pIsDetail);
  }

  /**
   * Creates a copy of an existing {@link IMapBean}.
   *
   * @param pOriginalMapBean the original map bean to copy
   * @param <KEY>            the type of the map's keys
   * @param <VALUE>          the type of the map's values
   * @return a copy of the map bean
   */
  static <KEY, VALUE> IMapBean<KEY, VALUE> createCopy(IMapBean<KEY, VALUE> pOriginalMapBean)
  {
    if (!(pOriginalMapBean instanceof MapBean))
      throw new OJRuntimeException("Only map beans created by this interface can be used for copying!");

    return new MapBean<>((MapBean<KEY, VALUE>) pOriginalMapBean);
  }

  @Override
  default void clear()
  {
    IBean.super.clear();
  }
}
