package de.adito.ojcms.transactions.annotations;

/**
 * Enumerates all possible transaction modes.
 *
 * @author Simon Danner, 25.12.2019
 */
public enum ETransactionMode
{
  /**
   * The annotated transaction scope requires a transaction, but does not enforce a new one.
   */
  REQUIRES,

  /**
   * The annotated transaction scope explicitly requires a new transaction.
   */
  REQUIRES_NEW
}
