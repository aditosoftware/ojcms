package de.adito.ojcms.beans.literals.fields.types;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.annotations.internal.TypeDefaultField;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.ECopyMode;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A bean field that holds a {@link Map}.
 * The original map will be transformed into a bean, which represents the mapping by its fields and associated values.
 *
 * @param <KEY>   the key type of the map
 * @param <VALUE> the value type of the map
 * @author Simon Danner, 01.02.2017
 */
@TypeDefaultField(types = IMapBean.class)
@GenericBeanField(genericWrapperType = IMapBean.class)
public class MapField<KEY, VALUE> extends AbstractField<IMapBean<KEY, VALUE>>
{
  private final Map<KEY, IField<?>> fieldCache = new ConcurrentHashMap<>();

  protected MapField(Class<IMapBean<KEY, VALUE>> pType, @NotNull String pName, Collection<Annotation> pAnnotations, boolean pIsOptional,
                     boolean pIsPrivate)
  {
    super(pType, pName, pAnnotations, pIsOptional, pIsPrivate);
  }

  /**
   * Creates a bean from a map.
   * he key's toString representation defines the bean field's name.
   * Each entry in the map will result in one bean field with the associated value.
   * This method is also able to define a field predicate, which excludes certain bean fields / map values.
   *
   * @param pMap       the map that will be transformed
   * @param pValueType the value type of the map
   * @return a (modifiable) bean, that represents the original map
   */
  public IMapBean<KEY, VALUE> createBeanFromMap(Map<KEY, VALUE> pMap, Class<VALUE> pValueType)
  {
    return IMapBean.createFromMap(pMap, pValueType, fieldCache::put, pKey -> Optional.ofNullable(fieldCache.get(pKey)),
                                  hasAnnotation(Detail.class));
  }

  /**
   * Transforms the bean back to a normal map.
   * The map will be a {@link LinkedHashMap}.
   *
   * @param pBean the bean that this field belongs to
   * @return the original map that was represented by the bean (new instance of the map)
   */
  public Map<KEY, VALUE> createMapFromBean(IBean pBean)
  {
    return new LinkedHashMap<>(pBean.getValue(this));
  }

  @Override
  public IMapBean<KEY, VALUE> copyValue(IMapBean<KEY, VALUE> pMapBean, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return pMapBean.createCopy(pMode, IMapBean::createCopy, pCustomFieldCopies);
  }
}
