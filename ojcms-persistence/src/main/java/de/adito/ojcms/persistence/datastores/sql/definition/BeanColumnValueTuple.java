package de.adito.ojcms.persistence.datastores.sql.definition;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.fields.util.*;
import de.adito.ojcms.sqlbuilder.definition.*;

import java.lang.reflect.Array;
import java.util.function.Function;
import java.util.stream.Stream;

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
   * @param pFieldValueTuple the field value tuple its based on
   */
  public BeanColumnValueTuple(FieldValueTuple<VALUE> pFieldValueTuple)
  {
    fieldValueTuple = pFieldValueTuple;
    column = IColumnIdentification.of(fieldValueTuple.getField().getName(), fieldValueTuple.getField().getDataType());
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
  protected static <TUPLE extends IColumnValueTuple<?>> TUPLE[] ofMultiple(Stream<FieldValueTuple<?>> pBeanTupleSource, Class<TUPLE> pTupleType,
                                                                           Function<FieldValueTuple<?>, TUPLE> pTupleMapper)
  {
    //noinspection unchecked
    return pBeanTupleSource
        .map(pTupleMapper)
        .toArray(pSize -> (TUPLE[]) Array.newInstance(pTupleType, pSize));
  }
}
