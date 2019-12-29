package de.adito.ojcms.transactions;

import de.adito.ojcms.transactions.api.ContainerIndexKey;

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
   * Determines if the content of a bean container (size related) is currently changed by an other transaction.
   *
   * @param pContainerId   the id of the container to check
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   * @return <tt>true</tt> if size related data of the container has been changed by another transaction
   */
  boolean isContainerDirty(String pContainerId, TransactionalChanges pSelfReference)
  {
    return _checkForChange(pChanges -> pChanges.isContainerDirty(pContainerId), pSelfReference);
  }

  /**
   * Determines if the data of a bean within a container has been changed by another transaction.
   *
   * @param pKey           the index based key identifying the bean in the container
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   * @return <tt>true</tt> the data of the bean within the container has been changed by another transaction
   */
  boolean isBeanInContainerDirty(ContainerIndexKey pKey, TransactionalChanges pSelfReference)
  {
    return _checkForChange(pChanges -> pChanges.isBeanInContainerDirty(pKey), pSelfReference);
  }

  /**
   * Determines if the data of a single bean has been changed by another transaction.
   *
   * @param pKey           the id of the single bean
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   * @return <tt>true</tt> the data of the single bean has been changed by another transaction
   */
  boolean isSingleBeanDirty(String pKey, TransactionalChanges pSelfReference)
  {
    return _checkForChange(pChanges -> pChanges.isSingleBeanDirty(pKey), pSelfReference);
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
   * Checks all registered {@link TransactionalChanges} instances (apart from the self reference) for changes based on a predicate.
   *
   * @param pPredicate     the predicate to determine if requested data is changed
   * @param pSelfReference a self reference to the asking changes instance (to exclude its changes)
   * @return <tt>true</tt> if the requested data has been changed by another transaction
   */
  private boolean _checkForChange(Predicate<TransactionalChanges> pPredicate, TransactionalChanges pSelfReference)
  {
    return activeTransactionalChanges.stream()
        .filter(pChanges -> pChanges.equals(pSelfReference))
        .anyMatch(pPredicate);
  }
}
