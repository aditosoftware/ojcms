package de.adito.ojcms.sql.datasource.model.column;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
import de.adito.ojcms.sqlbuilder.definition.condition.*;

import java.util.*;

/**
 * A where condition for database statements based on a {@link FieldValueTuple}.
 * This class extends the {@link BeanColumnValueTuple} by the operator for a condition.
 *
 * @param <VALUE> the data type of the value of the tuple
 * @author Simon Danner, 06.06.2018
 */
public class BeanWhereCondition<VALUE> extends BeanColumnValueTuple<VALUE> implements IWhereCondition<VALUE>
{
  private final IWhereOperator operator;
  private boolean negated = false;

  /**
   * Creates a new where condition.
   *
   * @param pField    the bean field the condition is based on
   * @param pValue    the associated bean value the condition is based on
   * @param pOperator the operator for the condition
   */
  public BeanWhereCondition(IField<VALUE> pField, VALUE pValue, IWhereOperator pOperator)
  {
    super(pField, pValue);
    operator = Objects.requireNonNull(pOperator);
  }

  @Override
  public IWhereOperator getOperator()
  {
    return operator;
  }

  @Override
  public BeanWhereCondition<VALUE> not()
  {
    negated = true;
    return this;
  }

  @Override
  public boolean isNegated()
  {
    return negated;
  }

  /**
   * Creates an array of where conditions based on a field value map.
   *
   * @param pValueMap the values mapped by bean fields to create the conditions of
   * @return an array of where conditions
   */
  public static BeanWhereCondition<?>[] ofMap(Map<IField<?>, Object> pValueMap)
  {
    //noinspection unchecked
    return pValueMap.entrySet().stream()
        .map(pEntry -> new BeanWhereCondition<>((IField) pEntry.getKey(), pEntry.getValue(), IWhereOperator.isEqual()))
        .toArray(BeanWhereCondition[]::new);
  }
}
