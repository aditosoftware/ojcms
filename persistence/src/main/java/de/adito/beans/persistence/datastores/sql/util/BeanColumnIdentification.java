package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IField;
import de.adito.beans.persistence.datastores.sql.builder.util.IColumnIdentification;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A SQL column identification based on a bean field.
 *
 * @param <TYPE> the data type of the column and according bean field
 * @author Simon Danner, 07.05.2018
 */
public class BeanColumnIdentification<TYPE> implements IColumnIdentification<TYPE>
{
  private final IField<TYPE> field;
  private final SQLSerializer serializer;

  /**
   * Creates a new column identification.
   *
   * @param pField      the bean field, that identifies the column
   * @param pSerializer a SQL serializer
   */
  public BeanColumnIdentification(IField<TYPE> pField, SQLSerializer pSerializer)
  {
    field = pField;
    serializer = pSerializer;
  }

  /**
   * The original bean field of this identification.
   *
   * @return the bean field
   */
  public IField<TYPE> getBeanField()
  {
    return field;
  }

  @Override
  public String getColumnName()
  {
    return field.getName();
  }

  @Override
  public TYPE fromSerial(String pSerial)
  {
    return serializer.fromPersistent(field, pSerial);
  }

  /**
   * Creates an array of column identifications based on a collection of bean fields.
   *
   * @param pFields     the bean fields to create the identifications tuples from
   * @param pSerializer a SQL serializer
   * @return an array of column identifications
   */
  public static List<BeanColumnIdentification<?>> of(Collection<IField<?>> pFields, SQLSerializer pSerializer)
  {
    return pFields.stream()
        .map(pField -> new BeanColumnIdentification<>(pField, pSerializer))
        .collect(Collectors.toList());
  }
}
