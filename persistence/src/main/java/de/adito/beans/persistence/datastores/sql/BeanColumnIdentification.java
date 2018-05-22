package de.adito.beans.persistence.datastores.sql;

import de.adito.beans.core.IField;
import de.adito.beans.persistence.datastores.sql.builder.util.IColumnIdentification;

import java.util.*;
import java.util.function.Function;
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

  public BeanColumnIdentification(IField<TYPE> pField)
  {
    field = pField;
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
    return SQLSerializer.fromPersistent(field, pSerial);
  }

  /**
   * Creates an array of column identifications based on a collection of bean fields.
   *
   * @param pFields the bean fields to create the identifications tuples from
   * @return an array of column identifications
   */
  public static List<BeanColumnIdentification<?>> of(Collection<IField<?>> pFields)
  {
    return pFields.stream()
        .map((Function<IField<?>, BeanColumnIdentification<?>>) BeanColumnIdentification::new)
        .collect(Collectors.toList());
  }
}
