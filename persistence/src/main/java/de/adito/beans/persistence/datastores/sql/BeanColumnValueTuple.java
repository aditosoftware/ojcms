package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.IBean;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.persistence.datastores.sql.builder.util.*;

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
  private final IColumnDefinition columnDefinition;

  public BeanColumnValueTuple(FieldTuple<TYPE> pFieldTuple)
  {
    fieldTuple = pFieldTuple;
    columnDefinition = IColumnDefinition.of(fieldTuple.getField().getName(), EColumnType.VARCHAR, 255);
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
    return SQLSerializer.toPersistent(fieldTuple);
  }

  /**
   * Creates an array of tuples based on a bean and its field value tuples.
   *
   * @param pBean the bean to create the tuples from
   * @return an array of database tuples
   */
  public static BeanColumnValueTuple[] of(IBean<?> pBean)
  {
    return _of(pBean.stream());
  }

  /**
   * Creates an array of tuples based on a bean and its identifier field value tuples
   *
   * @param pBean the bean to create the identifier tuples from
   * @return an array of database tuples
   */
  public static BeanColumnValueTuple[] ofBeanIdentifiers(IBean<?> pBean)
  {
    return _of(pBean.getIdentifiers().stream());
  }

  /**
   * Creates an array of tuples based on a some field value tuples.
   *
   * @param pTupleSource a stream of bean tuples
   * @return an array of database tuples
   */
  private static BeanColumnValueTuple[] _of(Stream<FieldTuple<?>> pTupleSource)
  {
    return pTupleSource
        .map((Function<FieldTuple<?>, BeanColumnValueTuple>) BeanColumnValueTuple::new)
        .toArray(BeanColumnValueTuple[]::new);
  }
}
