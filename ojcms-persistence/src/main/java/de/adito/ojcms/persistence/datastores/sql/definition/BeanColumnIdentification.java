package de.adito.ojcms.persistence.datastores.sql.definition;

import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.IBeanFieldBased;
import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A SQL column identification based on a bean field.
 *
 * @param <VALUE> the data type of the column and according bean field
 * @author Simon Danner, 07.05.2018
 */
public class BeanColumnIdentification<VALUE> implements IColumnIdentification<VALUE>, IBeanFieldBased<VALUE>
{
  private final IField<VALUE> field;

  /**
   * Creates a new column identification.
   *
   * @param pField the bean field, that identifies the column
   */
  public BeanColumnIdentification(IField<VALUE> pField)
  {
    field = pField;
  }

  @Override
  public IField<VALUE> getBeanField()
  {
    return field;
  }

  @Override
  public String getColumnName()
  {
    return field.getName();
  }

  @Override
  public Class<VALUE> getDataType()
  {
    return field.getDataType();
  }

  /**
   * Creates an array of column identifications based on a collection of bean fields.
   *
   * @param pFields the bean fields to create the identifications tuples from
   * @return an array of column identifications
   */
  public static List<BeanColumnIdentification<?>> ofMultiple(Collection<IField<?>> pFields)
  {
    return pFields.stream()
        .map((Function<IField<?>, ? extends BeanColumnIdentification<?>>) BeanColumnIdentification::new)
        .collect(Collectors.toList());
  }
}
