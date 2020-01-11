package de.adito.ojcms.transactions.exceptions;

import de.adito.ojcms.transactions.api.IBeanKey;

/**
 * Indicates that some persistent bean data could not have been found.
 *
 * @author Simon Danner, 01.01.2020
 */
public class BeanDataNotFoundException extends RuntimeException
{
  /**
   * Initializes the exception.
   *
   * @param pKey the key for which the persistent data could not have been found
   */
  public BeanDataNotFoundException(IBeanKey pKey)
  {
    super("Unable to find any bean data for key: " + pKey);
  }
}
