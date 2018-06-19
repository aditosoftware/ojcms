package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IField;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;

import java.util.Collection;

/**
 * A bean column definition based on a bean field
 *
 * @param <TYPE> the data type of the field
 * @author Simon Danner, 17.05.2018
 */
public class BeanColumnDefinition<TYPE> implements IColumnDefinition<TYPE>
{
  private final IField<TYPE> beanField;

  public BeanColumnDefinition(IField<TYPE> pBeanField)
  {
    beanField = pBeanField;
  }

  @Override
  public String getColumnName()
  {
    return beanField.getName();
  }

  @Override
  public EColumnType getColumnType()
  {
    return EColumnType.VARCHAR;
  }

  @Override
  public Class<TYPE> getDataType()
  {
    return beanField.getType();
  }

  @Override
  public int getColumnSize()
  {
    return 255;
  }

  /**
   * The bean field this column is based on.
   *
   * @return a bean field
   */
  public IField<TYPE> getBeanField()
  {
    return beanField;
  }

  /**
   * Creates an array of column definitions from a collection of {@link BeanColumnIdentification}.
   *
   * @param pColumnIdentifications the column identifications to create the array from
   * @return an array of columns definitions
   */
  public static BeanColumnDefinition[] ofMultiple(Collection<BeanColumnIdentification<?>> pColumnIdentifications)
  {
    return pColumnIdentifications.stream()
        .map(BeanColumnIdentification::getBeanField)
        .map(BeanColumnDefinition::new)
        .toArray(BeanColumnDefinition[]::new);
  }
}
