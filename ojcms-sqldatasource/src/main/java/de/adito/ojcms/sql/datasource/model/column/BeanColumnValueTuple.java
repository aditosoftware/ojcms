package de.adito.ojcms.sql.datasource.model.column;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.*;
import de.adito.ojcms.sqlbuilder.definition.*;

import java.util.Map;

/**
 * A database column value tuple based on a {@link FieldValueTuple}.
 *
 * @param <VALUE> the data type of the tuple
 * @author Simon Danner, 07.05.2018
 */
public class BeanColumnValueTuple<VALUE> implements IColumnValueTuple<VALUE>, IBeanFieldTupleBased<VALUE>
{
  private final FieldValueTuple<VALUE> fieldValueTuple;
  private final IColumnIdentification<VALUE> column;

  /**
   * Creates a new column value tuple.
   *
   * @param pField the bean field of the tuple
   * @param pValue the value of the tuple
   */
  public BeanColumnValueTuple(IField<VALUE> pField, VALUE pValue)
  {
    fieldValueTuple = new FieldValueTuple<>(pField, pValue);
    column = IColumnIdentification.of(pField.getName(), pField.getDataType());
  }

  @Override
  public IColumnIdentification<VALUE> getColumn()
  {
    return column;
  }

  @Override
  public VALUE getValue()
  {
    return fieldValueTuple.getValue();
  }

  @Override
  public FieldValueTuple<VALUE> getFieldValueTuple()
  {
    return fieldValueTuple;
  }

  /**
   * Creates an array of tuples based on a field value map.
   *
   * @param pValueMap the values mapped by bean fields to create the tuples of
   * @return an array of column value tuples
   */
  public static BeanColumnValueTuple<?>[] ofMap(Map<IField<?>, Object> pValueMap)
  {
    //noinspection unchecked
    return pValueMap.entrySet().stream()
        .map(pEntry -> new BeanColumnValueTuple<>((IField) pEntry.getKey(), pEntry.getValue()))
        .toArray(BeanColumnValueTuple[]::new);
  }
}
