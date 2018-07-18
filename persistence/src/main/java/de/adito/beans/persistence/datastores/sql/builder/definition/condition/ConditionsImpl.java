package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.IValueSerializer;
import de.adito.beans.persistence.datastores.sql.builder.definition.format.IValueStatementFormat;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.*;

/**
 * Implementation for multiple, concatenated where conditions.
 * This class holds the added conditions and the concatenation types.
 *
 * @author Simon Danner, 09.06.2018
 */
class ConditionsImpl extends AbstractNegatable<IWhereConditions> implements IWhereConditions
{
  private final Map<IValueStatementFormat, EConcatenationType> conditions = new HashMap<>();
  private IValueStatementFormat lastEntry;

  /**
   * Creates a multiple where condition.
   *
   * @param pCondition the initial condition to start from
   */
  ConditionsImpl(IValueStatementFormat pCondition)
  {
    lastEntry = pCondition;
  }

  @Override
  public <TYPE> IWhereConditions and(IWhereCondition<TYPE> pCondition)
  {
    return _add(pCondition, EConcatenationType.AND);
  }

  @Override
  public IWhereConditions and(IWhereConditions pMultipleConditions)
  {
    return _add(pMultipleConditions, EConcatenationType.AND);
  }

  @Override
  public <TYPE> IWhereConditions or(IWhereCondition<TYPE> pCondition)
  {
    return _add(pCondition, EConcatenationType.OR);
  }

  @Override
  public IWhereConditions or(IWhereConditions pMultipleConditions)
  {
    return _add(pMultipleConditions, EConcatenationType.OR);
  }

  @Override
  public String toStatementFormat(IValueSerializer pSerializer)
  {
    return (isNegated() ? "NOT " : "") + "(" + stream()
        .map(pCondition -> pCondition.toStatementFormat(pSerializer) + " " +
            (conditions.containsKey(pCondition) ? conditions.get(pCondition).name() + " " : ""))
        .collect(Collectors.joining()) + ")";
  }

  @NotNull
  @Override
  public Iterator<IValueStatementFormat> iterator()
  {
    return Stream.concat(conditions.keySet().stream(), Stream.of(lastEntry)).iterator();
  }

  /**
   * Adds a condition.
   * The new condition will be the last entry.
   * The old last entry will be stored in the map with the according concatenation type.
   *
   * @param pCondition         the where condition to add
   * @param pConcatenationType the concatenation type for the last condition
   * @return the concatenated condition itself for a pipelining mechanism
   */
  private IWhereConditions _add(IValueStatementFormat pCondition, EConcatenationType pConcatenationType)
  {
    conditions.put(lastEntry, pConcatenationType);
    lastEntry = pCondition;
    return this;
  }
}
