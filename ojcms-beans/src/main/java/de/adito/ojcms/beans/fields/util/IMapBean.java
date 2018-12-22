package de.adito.ojcms.beans.fields.util;

import de.adito.ojcms.beans.IBean;

import java.util.Map;

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
public interface IMapBean<KEY, VALUE> extends Map<KEY, VALUE>, IBean<IMapBean<KEY, VALUE>>
{
  @Override
  default void clear()
  {
    IBean.super.clear();
  }
}
