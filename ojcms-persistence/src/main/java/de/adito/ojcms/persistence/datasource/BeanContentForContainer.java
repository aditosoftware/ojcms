package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.*;

import java.util.Map;

/**
 * Manages the content of a persistent bean within a container for an active {@link ITransaction}.
 *
 * @param <BEAN> the type of the bean in the container the content is for
 * @author Simon Danner, 01.03.2020
 */
class BeanContentForContainer<BEAN extends IBean> extends AbstractBeanContent<CurrentIndexKey, BEAN>
{
  /**
   * Initializes the bean content by requesting the initial content by the given key.
   * The bean type this content is for will be requested from the transaction as well.
   *
   * @param pKey         the key to identify the bean content in the container by index
   * @param pTransaction the transaction this bean data is associated with
   */
  BeanContentForContainer(CurrentIndexKey pKey, ITransaction pTransaction)
  {
    super(pKey, pTransaction.requestBeanTypeWithinContainer(pKey), pTransaction, pTransaction.requestBeanDataByIndex(pKey).getData());
  }

  /**
   * Initializes the bean content by requesting the initial content by the given key.
   *
   * @param pKey         the key to identify the bean content in the container by index
   * @param pBeanType    the type of the bean the content is for
   * @param pTransaction the transaction this bean data is associated with
   */
  BeanContentForContainer(CurrentIndexKey pKey, Class<BEAN> pBeanType, ITransaction pTransaction)
  {
    super(pKey, pBeanType, pTransaction, pTransaction.requestBeanDataByIndex(pKey).getData());
  }

  /**
   * Initializes the bean content with given bean data.
   * The bean type this content is for will be requested from the transaction.
   *
   * @param pKey         the key to identify the bean content in the container by index
   * @param pTransaction the transaction this bean data is associated with
   * @param pContent     given initial content mapped by bean fields
   */
  BeanContentForContainer(CurrentIndexKey pKey, ITransaction pTransaction, Map<IField<?>, Object> pContent)
  {
    super(pKey, pTransaction.requestBeanTypeWithinContainer(pKey), pTransaction, pContent);
  }

  /**
   * Initializes the bean content with given bean data.
   *
   * @param pKey         the key to identify the bean content in the container by index
   * @param pBeanType    the type of the bean the content is for
   * @param pTransaction the transaction this bean data is associated with
   * @param pContent     given initial content mapped by bean fields
   */
  BeanContentForContainer(CurrentIndexKey pKey, Class<BEAN> pBeanType, ITransaction pTransaction, Map<IField<?>, Object> pContent)
  {
    super(pKey, pBeanType, pTransaction, pContent);
  }

  @Override
  <VALUE> void registerValueChangeAtTransaction(ITransaction pTransaction, CurrentIndexKey pKey, IField<VALUE> pChangedField,
                                                VALUE pNewValue)
  {
    pTransaction.registerContainerBeanValueChange(pKey, pChangedField, pNewValue);
  }
}
