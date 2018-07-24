package de.adito.beans.persistence.datastores.sql.builder.definition.condition;

import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import de.adito.beans.persistence.datastores.sql.builder.format.IPreparedStatementFormat;

import java.util.*;

/**
 * Wrapper class for a condition operator and an id value for a where condition for id columns.
 * It can be presented on a condition statement format.
 * Because the format depends on the global id column name, the operator and the value have to be stored here
 * until the format will be build finally, where the id column name is available.
 *
 * @author Simon Danner, 21.07.2018
 */
class OperatorWithId extends AbstractNegatable<OperatorWithId> implements IPreparedStatementFormat
{
  private final IWhereOperator operator;
  private final int id;
  private IColumnIdentification<Integer> identification;

  /**
   * Creates a new wrapper instance.
   *
   * @param pOperator the where operator for the id condition
   * @param pId       the id value for the condition
   */
  OperatorWithId(IWhereOperator pOperator, int pId)
  {
    operator = pOperator;
    id = pId;
  }

  @Override
  public String toStatementFormat(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return IWhereCondition.of(_getIdentification(pIdColumnName), id, operator).toStatementFormat(pDatabaseType, pIdColumnName);
  }

  @Override
  public List<IColumnValueTuple<?>> getArguments(EDatabaseType pDatabaseType, String pIdColumnName)
  {
    return Collections.singletonList(IColumnValueTuple.of(_getIdentification(pIdColumnName), id));
  }

  /**
   * The id column identification for the where condition.
   * Creates the identification initially or return an already created instance.
   *
   * @param pIdColumnName the global id column name
   * @return the column identification for the condition
   */
  private IColumnIdentification<Integer> _getIdentification(String pIdColumnName)
  {
    return identification == null ? (identification = IColumnIdentification.of(pIdColumnName, Integer.class)) : identification;
  }
}
