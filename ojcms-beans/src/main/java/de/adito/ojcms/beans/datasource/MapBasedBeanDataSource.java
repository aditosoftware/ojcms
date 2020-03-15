package de.adito.ojcms.beans.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * A hash map based implementation of {@link IBeanDataSource}.
 * Used as default data source for beans.
 *
 * @author Simon Danner, 08.12.2018
 */
public final class MapBasedBeanDataSource implements IBeanDataSource
{
  private final Map<IField<?>, Object> values;

  /**
   * Creates the map based data source.
   *
   * @param pFields a list of fields for this data source
   */
  public MapBasedBeanDataSource(List<IField<?>> pFields)
  {
    values = pFields.stream()
        .collect(HashMap::new, (pMap, pField) -> pMap.put(pField, pField.getInitialValue()), Map::putAll);
  }

  /**
   * Creates the data source based on an existing bean.
   *
   * @param pBean the bean to take the initial fields and values from
   */
  public MapBasedBeanDataSource(IBean pBean)
  {
    values = pBean.stream().collect(toMap(FieldValueTuple::getField,
                                          pTuple -> pTuple.getValue() == null ? pTuple.getField().getInitialValue() : pTuple.getValue()));
  }

  @Override
  public <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    //noinspection unchecked
    return (VALUE) values.get(pField);
  }

  @Override
  public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue, boolean pAllowNewField)
  {
    final boolean existing = values.containsKey(pField);
    if (!pAllowNewField && !existing)
      throw new OJInternalException("It is not allowed to add new fields for this bean data core. field: " + pField.getName());
    values.put(pField, pValue == null ? pField.getInitialValue() : pValue);
  }

  @Override
  public <VALUE> void removeField(IField<VALUE> pField)
  {
    values.remove(pField);
  }
}
