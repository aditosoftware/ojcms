package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;

import java.util.Map;

/**
 * Manages the content of a persistent bean within a container for an active {@link ITransaction}.
 *
 * @author Simon Danner, 01.03.2020
 */
class BeanContentForContainer extends AbstractBeanContent<CurrentIndexKey>
{
  /**
   * Initializes the bean content by requesting the initial content by the given key.
   *
   * @param pTransaction the transaction this bean data is associated with
   */
  BeanContentForContainer(CurrentIndexKey pKey, ITransaction pTransaction)
  {
    super(pKey, pTransaction, pTransaction.requestBeanDataByIndex(pKey).getData());
  }

  /**
   * Initializes the bean content with given bean data.
   *
   * @param pTransaction the transaction this bean data is associated with
   * @param pContent     given initial content mapped by bean fields
   */
  BeanContentForContainer(CurrentIndexKey pKey, ITransaction pTransaction, Map<IField<?>, Object> pContent)
  {
    super(pKey, pTransaction, pContent);
  }

  @Override
  <VALUE> void registerValueChangeAtTransaction(ITransaction pTransaction, CurrentIndexKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    pTransaction.registerContainerBeanValueChange(pKey, pChangedField, pNewValue);
  }
}
