package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.datasource.IBeanDataSource;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.annotations.TransactionalScoped;

/**
 * A {@link IBeanDataSource} for persistent beans.
 * The actual values are provided by a {@link TransactionalScoped} {@link BeanContent}.
 * Field removal is not supported for this datasource.
 *
 * @author Simon Danner, 01.01.2020
 */
class PersistentBeanDatasource implements IBeanDataSource
{
  private final BeanContent content;

  /**
   * Initializes the persistent bean datasource.
   *
   * @param pContent the transactional scoped content of the bean
   */
  PersistentBeanDatasource(BeanContent pContent)
  {
    content = pContent;
  }

  @Override
  public <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    //noinspection unchecked
    return (VALUE) content.getValue(pField);
  }

  @Override
  public <VALUE> void setValue(IField<VALUE> pField, VALUE pValue, boolean pAllowNewField)
  {
    content.setValue(pField, pValue);
  }

  @Override
  public <VALUE> void removeField(IField<VALUE> pField)
  {
    throw new UnsupportedOperationException("The addition of new bean fields is not allowed for persistent beans!");
  }
}
