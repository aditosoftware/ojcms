package de.adito.ojcms.persistence.datastores.sql.definition;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
import de.adito.ojcms.sqlbuilder.definition.condition.*;

import java.util.Objects;
import java.util.function.Function;

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
   * @param pFieldValueTuple the bean field value tuple its based on
   * @param pOperator        the operator for the condition
   */
  public BeanWhereCondition(FieldValueTuple<VALUE> pFieldValueTuple, IWhereOperator pOperator)
  {
    super(pFieldValueTuple);
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
  public static BeanWhereCondition<?>[] ofBean(IBean<?> pBean, Function<FieldValueTuple<?>, IWhereOperator> pOperatorResolver)
  {
    return ofMultiple(pBean.stream(), BeanWhereCondition.class, pBeanTuple ->
        new BeanWhereCondition<>(pBeanTuple, pOperatorResolver.apply(pBeanTuple)));
  }

  /**
   * Creates an array of where conditions on a bean and its identifier field value tuples.
   *
   * @param pBean             the bean to create the identifier tuples from
   * @param pOperatorResolver a function to resolve the where operator from the bean field tuple
   * @return an array of where conditions
   */
  public static BeanWhereCondition<?>[] ofBeanIdentifiers(IBean<?> pBean, Function<FieldValueTuple<?>, IWhereOperator> pOperatorResolver)
  {
    return ofMultiple(pBean.getIdentifiers().stream(), BeanWhereCondition.class,
                      pBeanTuple -> new BeanWhereCondition<>(pBeanTuple, pOperatorResolver.apply(pBeanTuple)));
  }
}
