package de.adito.ojcms.sql.datasource.model.column;

import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.IBeanFieldBased;
import de.adito.ojcms.sqlbuilder.definition.column.*;

import java.util.Collection;

/**
 * A database column definition based on a bean field.
 *
 * @param <VALUE> the data type of the field
 * @author Simon Danner, 17.05.2018
 */
public class BeanColumnDefinition<VALUE> implements IColumnDefinition, IBeanFieldBased<VALUE>
{
  private final IField<VALUE> beanField;
  private final IColumnType columnType;

  /**
   * Creates a column definition for a bean field.
   *
   * @param pBeanField the bean field the column is based on
   */
  public BeanColumnDefinition(IField<VALUE> pBeanField)
  {
    beanField = pBeanField;
    columnType = EColumnType.getByDataType(pBeanField.getDataType())
        .orElse(EColumnType.STRING.create());

    if (beanField.hasAnnotation(Identifier.class))
      columnType.primaryKey();
  }

  @Override
  public String getColumnName()
  {
    return beanField.getName();
  }

  @Override
  public IColumnType getColumnType()
  {
    return columnType;
  }

  /**
   * The bean field this column is based on.
   *
   * @return a bean field
   */
  @Override
  public IField<VALUE> getBeanField()
  {
    return beanField;
  }

  /**
   * Creates an array of column definitions from a collection of bean fields.
   *
   * @param pBeanFields the bean fields to create the array from
   * @return an array of columns definitions
   */
  public static BeanColumnDefinition<?>[] ofFields(Collection<IField<?>> pBeanFields)
  {
    return pBeanFields.stream()
        .map(BeanColumnDefinition::new)
        .toArray(BeanColumnDefinition<?>[]::new);
  }
}
