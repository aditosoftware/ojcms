package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IBean;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

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
  private final IColumnDefinition columnDefinition;
  private final SQLSerializer serializer;

  /**
   * Creates a new column value tuple.
   *
   * @param pFieldTuple the field value tuple its based on
   * @param pSerializer a SQL serializer
   */
  public BeanColumnValueTuple(FieldTuple<TYPE> pFieldTuple, SQLSerializer pSerializer)
  {
    fieldTuple = pFieldTuple;
    columnDefinition = IColumnDefinition.of(fieldTuple.getField().getName(), EColumnType.VARCHAR, 255);
    serializer = pSerializer;
  }

  @Override
  public IColumnDefinition getColumnDefinition()
  {
    return columnDefinition;
  }

  @Override
  public TYPE getValue()
  {
    return fieldTuple.getValue();
  }

  @Override
  public String toSerial()
  {
    return serializer.toPersistent(fieldTuple);
  }

  /**
   * Creates an array of tuples based on a bean and its field value tuples.
   *
   * @param pBean       the bean to create the tuples from
   * @param pSerializer a SQL serializer
   * @return an array of database tuples
   */
  public static BeanColumnValueTuple[] of(IBean<?> pBean, SQLSerializer pSerializer)
  {
    return _of(pBean.stream(), pSerializer);
  }

  /**
   * Creates an array of tuples based on a bean and its identifier field value tuples
   *
   * @param pBean       the bean to create the identifier tuples from
   * @param pSerializer a SQL serializer
   * @return an array of database tuples
   */
  public static BeanColumnValueTuple[] ofBeanIdentifiers(IBean<?> pBean, SQLSerializer pSerializer)
  {
    return _of(pBean.getIdentifiers().stream(), pSerializer);
  }

  /**
   * Creates an array of tuples based on a some field value tuples.
   *
   * @param pTupleSource a stream of bean tuples
   * @param pSerializer  a SQL serializer
   * @return an array of database tuples
   */
  private static BeanColumnValueTuple[] _of(Stream<FieldTuple<?>> pTupleSource, SQLSerializer pSerializer)
  {
    return pTupleSource
        .map(pTuple -> new BeanColumnValueTuple<>(pTuple, pSerializer))
        .toArray(BeanColumnValueTuple[]::new);
  }
}
