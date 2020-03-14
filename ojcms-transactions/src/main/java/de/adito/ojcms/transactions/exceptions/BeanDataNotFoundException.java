package de.adito.ojcms.transactions.exceptions;

import de.adito.ojcms.transactions.api.*;

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
  public BeanDataNotFoundException(InitialIndexKey pKey)
  {
    super("Unable to find container bean data for key: " + pKey);
  }

  /**
   * Initializes the exception.
   *
   * @param pKey the key for which the persistent data could not have been found
   */
  public BeanDataNotFoundException(SingleBeanKey pKey)
  {
    super("Unable to find single bean for id: " + pKey.getBeanId());
  }
}
