package de.adito.ojcms.sql.datasource.model.column;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.IBeanFieldBased;
import de.adito.ojcms.sqlbuilder.definition.IColumnIdentification;

/**
 * A database column identification based on a bean field.
 *
 * @param <VALUE> the data type of the column and associated bean field
 * @author Simon Danner, 07.05.2018
 */
public class BeanColumnIdentification<VALUE> implements IColumnIdentification<VALUE>, IBeanFieldBased<VALUE>
{
  private final IField<VALUE> field;

  /**
   * Creates a new column identification.
   *
   * @param pField the bean field that identifies the column
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
}
