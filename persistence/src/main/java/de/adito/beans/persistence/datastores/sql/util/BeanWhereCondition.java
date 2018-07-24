package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IBean;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.persistence.datastores.sql.builder.definition.condition.*;

import java.util.Objects;
import java.util.function.Function;

/**
 * A where condition for database SQL statements based on a bean {@link FieldTuple}.
 * This class extends the {@link BeanColumnValueTuple} by the operator for a condition.
 *
 * @author Simon Danner, 06.06.2018
 */
public class BeanWhereCondition<TYPE> extends BeanColumnValueTuple<TYPE> implements IWhereCondition<TYPE>
{
  private final IWhereOperator operator;
  private boolean negated = false;

  /**
   * Creates a new where condition.
   *
   * @param pFieldTuple the bean field value tuple its based on
   * @param pOperator   the operator for the condition
   */
  public BeanWhereCondition(FieldTuple<TYPE> pFieldTuple, IWhereOperator pOperator)
  {
    super(pFieldTuple);
    operator = Objects.requireNonNull(pOperator);
  }

  @Override
  public IWhereOperator getOperator()
  {
    return operator;
  }

  @Override
  public BeanWhereCondition<TYPE> not()
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
   * Creates an array of where conditions based on a bean and its field value tuples.
   * These conditions uses the default where operator "=".
   *
   * @param pBean the bean to create the tuples from
   * @return an array of where conditions
   */
  public static BeanWhereCondition<?>[] ofBean(IBean<?> pBean)
  {
    return ofBean(pBean, pTuple -> IWhereOperator.isEqual());
  }

  /**
   * Creates an array of where conditions on a bean and its identifier field value tuples.
   * These conditions uses the default where operator "=".
   *
   * @param pBean the bean to create the identifier tuples from
   * @return an array of where conditions
   */
  public static BeanWhereCondition<?>[] ofBeanIdentifiers(IBean<?> pBean)
  {
    return ofBeanIdentifiers(pBean, pTuple -> IWhereOperator.isEqual());
  }

  /**
   * Creates an array of where conditions based on a bean and its field value tuples.
   *
   * @param pBean             the bean to create the tuples from
   * @param pOperatorResolver a function to resolve the where operator from the bean field tuple
   * @return an array of where conditions
   */
  public static BeanWhereCondition<?>[] ofBean(IBean<?> pBean, Function<FieldTuple<?>, IWhereOperator> pOperatorResolver)
  {
    //noinspection unchecked
    return ofMultiple(pBean.stream(), BeanWhereCondition.class, pBeanTuple -> new BeanWhereCondition(pBeanTuple, pOperatorResolver.apply(pBeanTuple)));
  }

  /**
   * Creates an array of where conditions on a bean and its identifier field value tuples.
   *
   * @param pBean             the bean to create the identifier tuples from
   * @param pOperatorResolver a function to resolve the where operator from the bean field tuple
   * @return an array of where conditions
   */
  public static BeanWhereCondition<?>[] ofBeanIdentifiers(IBean<?> pBean, Function<FieldTuple<?>, IWhereOperator> pOperatorResolver)
  {
    //noinspection unchecked
    return ofMultiple(pBean.getIdentifiers().stream(), BeanWhereCondition.class,
                      pBeanTuple -> new BeanWhereCondition(pBeanTuple, pOperatorResolver.apply(pBeanTuple)));
  }
}
