package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;

/**
 * Manages the content of a persistent single bean for an active {@link ITransaction}.
 *
 * @param <BEAN> the type of the single bean the content is for
 * @author Simon Danner, 01.03.2020
 */
class SingleBeanContent<BEAN extends IBean> extends AbstractBeanContent<SingleBeanKey, BEAN>
{
  /**
   * Initializes the bean content with given bean data.
   *
   * @param pBeanKey     the bean key that identifies these data
   * @param pTransaction the transaction this bean data is associated with
   */
  SingleBeanContent(SingleBeanKey pBeanKey, Class<BEAN> pBeanType, ITransaction pTransaction)
  {
    super(pBeanKey, pBeanType, pTransaction, pTransaction.requestSingleBeanData(pBeanKey).getData());
  }

  @Override
  <VALUE> void registerValueChangeAtTransaction(ITransaction pTransaction, SingleBeanKey pKey, IField<VALUE> pChangedField, VALUE pNewValue)
  {
    pTransaction.registerSingleBeanValueChange(pKey, pChangedField, pNewValue);
  }
}
