package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;

/**
 * Manages the content of a persistent single bean for an active {@link ITransaction}.
 *
 * @author Simon Danner, 01.03.2020
 */
class SingleBeanContent extends AbstractBeanContent<SingleBeanKey>
{
  /**
   * Initializes the bean content with given bean data.
   *
   * @param pBeanKey     the bean key that identifies these data
   * @param pTransaction the transaction this bean data is associated with
   */
  SingleBeanContent(SingleBeanKey pBeanKey, ITransaction pTransaction)
  {
    super(pBeanKey, pTransaction, pTransaction.requestSingleBeanData(pBeanKey).getData());
  }

  @Override
  <VALUE> void registerValueChangeAtTransaction(ITransaction pTransaction, SingleBeanKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    pTransaction.registerSingleBeanValueChange(pKey, pChangedField, pNewValue);
  }
}
