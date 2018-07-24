package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.*;
import de.adito.beans.core.annotations.Identifier;
import de.adito.beans.core.fields.BeanField;
import de.adito.beans.persistence.*;
import de.adito.beans.persistence.datastores.sql.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.column.*;

import java.util.Collection;

/**
 * A bean column definition based on a bean field.
 *
 * @param <TYPE> the data type of the field
 * @author Simon Danner, 17.05.2018
 */
public class BeanColumnDefinition<TYPE> implements IColumnDefinition
{
  private final IField<TYPE> beanField;
  private final IColumnType columnType;

  /**
   * Creates a column definition for a bean field.
   *
   * @param pBeanField the bean field the column is based on
   */
  public BeanColumnDefinition(IField<TYPE> pBeanField)
  {
    beanField = pBeanField;
    columnType = EColumnType.getByDataType(pBeanField.getType())
        .orElse(EColumnType.STRING.create());
    if (beanField.hasAnnotation(Identifier.class))
      columnType.primaryKey();
    _setForeignKey();
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
  public IField<TYPE> getBeanField()
  {
    return beanField;
  }

  /**
   * Sets a foreign key for the column type, if it is a {@link BeanField} that refers to another persistent bean.
   */
  private void _setForeignKey()
  {
    if (!(beanField instanceof BeanField))
      return;

    //noinspection unchecked
    final Class<? extends IBean> beanType = (Class<? extends IBean>) beanField.getType();
    if (!beanType.isAnnotationPresent(Persist.class))
      throw new RuntimeException("A persistent bean can only create a reference to another persistent bean! type: " + beanType.getName());
    final Persist annotation = beanType.getAnnotation(Persist.class);
    String tableName = annotation.mode() == EPersistenceMode.SINGLE ? IDatabaseConstants.BEAN_TABLE_NAME : annotation.containerId();
    String columnName = annotation.mode() == EPersistenceMode.SINGLE ? IDatabaseConstants.BEAN_TABLE_BEAN_ID : IDatabaseConstants.ID_COLUMN;
    columnType.foreignKey(IForeignKey.of(tableName, columnName, pConnectionInfo -> {
      if (annotation.mode() == EPersistenceMode.SINGLE)
        SQLPersistentBean.createBeanTable(pConnectionInfo);
      else
        //noinspection unchecked
        SQLPersistentContainer.createTableForContainer(pConnectionInfo, beanType, tableName);
    }));
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
