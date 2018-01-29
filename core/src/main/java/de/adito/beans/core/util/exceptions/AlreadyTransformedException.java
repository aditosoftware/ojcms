package de.adito.beans.core.util.exceptions;

/**
 * Thrown, if an operation should take place before a bean transformation, but has already been performed.
 *
 * @author Simon Danner, 18.07.2017
 */
public class AlreadyTransformedException extends RuntimeException
{
  public AlreadyTransformedException(String pTransformableTypeName)
  {
    super("The component " + pTransformableTypeName + " has already been transformed!");
  }
}
