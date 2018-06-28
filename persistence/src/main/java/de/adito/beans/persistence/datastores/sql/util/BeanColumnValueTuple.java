package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IBean;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A SQL column value tuple based on a {@link FieldTuple}.
 *
 * @param <TYPE> the data type of the tuple
 * @author Simon Danner, 07.05.2018
 */
public class BeanColumnValueTuple<TYPE> implements IColumnValueTuple<TYPE>
{
  private final FieldTuple<TYPE> fieldTuple;
  private final IColumnIdentification<TYPE> column;

  /**
   * Creates a new column value tuple.
   *
   * @param pFieldTuple the field value tuple its based on
   */
  public BeanColumnValueTuple(FieldTuple<TYPE> pFieldTuple)
  {
    fieldTuple = pFieldTuple;
    column = IColumnIdentification.of(fieldTuple.getField().getName(), fieldTuple.getField().getType());
  }

  @Override
  public IColumnIdentification<TYPE> getColumn()
  {
    return column;
  }

  @Override
  public TYPE getValue()
  {
    return fieldTuple.getValue();
  }

  /**
   * The bean field tuple for this column tuple.
   *
   * @return a bean tuple
   */
  public FieldTuple<TYPE> getFieldTuple()
  {
    return fieldTuple;
  }

  /**
   * Creates an array of tuples based on a bean and its field value tuples.
   *
   * @param pBean the bean to create the tuples from
   * @return an array of database tuples
   */
  public static BeanColumnValueTuple<?>[] ofBean(IBean<?> pBean)
  {
    return ofMultiple(pBean.stream(), BeanColumnValueTuple.class, BeanColumnValueTuple::new);
  }

  /**
   * Creates an array of tuples based on a bean and its identifier field value tuples.
   *
   * @param pBean the bean to create the identifier tuples from
   * @return an array of database tuples
   */
  public static BeanColumnValueTuple<?>[] ofBeanIdentifiers(IBean<?> pBean)
  {
    return ofMultiple(pBean.getIdentifiers().stream(), BeanColumnValueTuple.class, BeanColumnValueTuple::new);
  }

  /**
   * Creates an array of generic column value tuples based on a some field value tuples.
   *
   * @param pBeanTupleSource a stream of bean tuples
   * @param pTupleType       the type of the database tuple
   * @param pTupleMapper     a function to map from a bean tuple to the generic database column value tuple
   * @return an array of generic database tuples
   */
  protected static <TUPLE extends IColumnValueTuple<?>> TUPLE[] ofMultiple(Stream<FieldTuple<?>> pBeanTupleSource, Class<TUPLE> pTupleType,
                                                                           Function<FieldTuple<?>, TUPLE> pTupleMapper)
  {
    //noinspection unchecked
    return pBeanTupleSource
        .map(pTupleMapper)
        .toArray(pSize -> (TUPLE[]) Array.newInstance(pTupleType, pSize));
  }
}
