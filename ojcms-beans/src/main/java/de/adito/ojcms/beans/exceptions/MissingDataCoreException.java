package de.adito.ojcms.beans.exceptions;

/**
 * Thrown, if a bean or bean container misses its encapsulated data core.
 *
 * @author Simon Danner, 26.12.2018
 */
public class MissingDataCoreException extends OJRuntimeException
{
  /**
   * Creates a new exception for a specific type missing the data core.
   *
   * @param pTypeMissingTheCore the type missing the encapsulated data core
   */
  public MissingDataCoreException(Class<?> pTypeMissingTheCore)
  {
    super("Missing data core for " + pTypeMissingTheCore.getName() + "! This element requires some data to work properly!");
  }
}
