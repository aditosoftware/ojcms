package de.adito.ojcms.persistence.datastores.sql.definition;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.types.BeanField;
import de.adito.ojcms.beans.literals.fields.util.IBeanFieldBased;
import de.adito.ojcms.persistence.*;
import de.adito.ojcms.persistence.datastores.sql.*;
import de.adito.ojcms.persistence.datastores.sql.util.DatabaseConstants;
import de.adito.ojcms.persistence.util.*;
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
  @Override
  public IField<VALUE> getBeanField()
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
    final Class<? extends IBean> beanType = (Class<? extends IBean>) beanField.getDataType();
    if (!beanType.isAnnotationPresent(Persist.class))
      throw new OJPersistenceException(beanType);
    final Persist annotation = beanType.getAnnotation(Persist.class);
    final String tableName = annotation.mode() == EPersistenceMode.SINGLE ? DatabaseConstants.BEAN_TABLE_NAME : annotation.containerId();
    final String columnName = annotation.mode() == EPersistenceMode.SINGLE ? DatabaseConstants.BEAN_TABLE_BEAN_ID :
        DatabaseConstants.ID_COLUMN;
    columnType.foreignKey(IForeignKey.of(tableName, columnName, pConnectionInfo -> {
      if (annotation.mode() == EPersistenceMode.SINGLE)
        SQLPersistentBeanSource.createBeanTable(pConnectionInfo);
      else
        //noinspection unchecked
        SQLPersistentContainerSource.createTableForContainer(pConnectionInfo, beanType, tableName);
    }));
  }

  /**
   * Creates an array of column definitions from a collection of {@link BeanColumnIdentification}.
   *
   * @param pColumnIdentifications the column identifications to create the array from
   * @return an array of columns definitions
   */
  public static BeanColumnDefinition<?>[] ofMultiple(Collection<BeanColumnIdentification<?>> pColumnIdentifications)
  {
    return pColumnIdentifications.stream()
        .map(BeanColumnIdentification::getBeanField)
        .map(BeanColumnDefinition::new)
        .toArray(BeanColumnDefinition<?>[]::new);
  }
}
