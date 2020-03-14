package de.adito.ojcms.persistence.datasource;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.transactions.api.ITransaction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Base class to manage the content of a persistent bean within one {@link ITransaction}.
 *
 * @author Simon Danner, 01.01.2020
 */
abstract class AbstractBeanContent<KEY>
{
  private final KEY beanKey;
  private final Map<IField<?>, Object> content;
  private final ITransaction transaction;

  /**
   * Initializes the bean content with given bean data.
   *
   * @param pBeanKey     the bean key that identifies these data
   * @param pTransaction the transaction this bean data is associated with
   * @param pContent     given initial content mapped by bean fields
   */
  AbstractBeanContent(KEY pBeanKey, ITransaction pTransaction, Map<IField<?>, Object> pContent)
  {
    beanKey = Objects.requireNonNull(pBeanKey);
    transaction = Objects.requireNonNull(pTransaction);
    content = new HashMap<>(pContent);
  }

  /**
   * Resolves the value for a bean field.
   *
   * @param pField  the bean field to resolve the value for
   * @param <VALUE> the data type of the bean field
   * @return the value associated with the bean field
   */
  @Nullable <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    //noinspection unchecked
    return (VALUE) content.get(pField);
  }

  /**
   * Sets a value for a bean field. The bean field has to exist, otherwise a runtime exception will be thrown.
   * Registers the value change at {@link ITransaction}.
   *
   * @param pField  the bean field to set the value for
   * @param pValue  the value to set for the bean field
   * @param <VALUE> the data type of the bean field
   */
  <VALUE> void setValue(IField<VALUE> pField, @Nullable VALUE pValue)
  {
    if (!content.containsKey(pField))
      throw new UnsupportedOperationException("Addition of fields not supported for persistent beans!");

    content.put(pField, pValue);
    registerValueChangeAtTransaction(transaction, beanKey, pField, pValue);
  }

  /**
   * Registers a bean value change at the associated transaction.
   *
   * @param pTransaction  the current transaction
   * @param pBeanKey      the bean key associated with this content
   * @param pChangedField the changed bean field
   * @param pValue        the new value
   */
  abstract <VALUE> void registerValueChangeAtTransaction(ITransaction pTransaction, KEY pBeanKey, IField<VALUE> pChangedField, VALUE pValue);
}
