package de.adito.ojcms.transactions;

import de.adito.ojcms.transactions.api.IBeanKey;
import de.adito.ojcms.transactions.exceptions.ConcurrentTransactionException;

import javax.enterprise.context.ApplicationScoped;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Application scoped registry for {@link TransactionalChanges}.
 * Each {@link TransactionalChanges} instances must register and deregister here.
 * Through this registry it is possible for a single transaction to determine if certain data is currently changed by another transaction.
 *
 * @author Simon Danner, 27.12.2019
 */
@ApplicationScoped
class OverallTransactionalChanges
{
  private final Set<TransactionalChanges> activeTransactionalChanges = ConcurrentHashMap.newKeySet();

  /**
   * Throws a {@link ConcurrentTransactionException} if a bean container has been modified by another transaction.
   *
   * @param pContainerId   the id of the container to check
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   */
  void throwIfContainerDirty(String pContainerId, TransactionalChanges pSelfReference)
  {
    _throwIfChangedInOtherTransaction(pContainerId, pChanges -> pChanges.isContainerDirty(pContainerId), pSelfReference);
  }

  /**
   * Throws a {@link ConcurrentTransactionException} if a bean has been modified by another transaction.
   *
   * @param pKey           the key identifying the bean
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   */
  <KEY extends IBeanKey> void throwIfBeanDirty(KEY pKey, TransactionalChanges pSelfReference)
  {
    _throwIfChangedInOtherTransaction(pKey, pChanges -> pChanges.isBeanDirty(pKey), pSelfReference);
  }

  /**
   * Registers a changes instance from a transaction at this overall registry.
   *
   * @param pChanges the transactional changes instance to register
   */
  void registerFromTransaction(TransactionalChanges pChanges)
  {
    activeTransactionalChanges.add(pChanges);
  }

  /**
   * Deregisters a changes instances (mainly when a transaction terminates).
   *
   * @param pChanges the transactional changes instance to deregister
   */
  void deregister(TransactionalChanges pChanges)
  {
    activeTransactionalChanges.remove(pChanges);
  }

  /**
   * Checks all registered {@link TransactionalChanges} instances (apart from the self reference) for changes based on a predicate
   * and throws a {@link ConcurrentTransactionException} if changes have been found.
   *
   * @param pKey           the key used in the predicate to identify a bean or container
   * @param pPredicate     the predicate to determine if requested data is changed
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   */
  private void _throwIfChangedInOtherTransaction(Object pKey, Predicate<TransactionalChanges> pPredicate, TransactionalChanges pSelfReference)
  {
    if (activeTransactionalChanges.stream()
        .filter(pChanges -> pChanges.equals(pSelfReference))
        .anyMatch(pPredicate))
      throw new ConcurrentTransactionException(pKey);
  }
}
