package de.adito.ojcms.transactions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * An application scoped transaction manager that is able to either commit or roll back the current {@link ManagedTransaction}.
 *
 * @author Simon Danner, 26.12.2019
 */
@ApplicationScoped
class TransactionManager
{
  @Inject
  private ManagedTransaction transaction;

  /**
   * Commits the changes of the current transaction to a persistent storage system.
   */
  void commitChanges()
  {
    transaction.commit();
  }

  /**
   * Rolls back the changes of the current transaction.
   */
  void rollbackChanges()
  {
    transaction.rollback();
  }
}
