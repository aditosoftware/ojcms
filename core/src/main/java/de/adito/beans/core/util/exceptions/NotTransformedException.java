package de.adito.beans.core.util.exceptions;

/**
 * Thrown, if an operation requires to take place after a bean transformation, but the transformation hasn't finished yet.
 *
 * @author Simon Danner, 18.07.2017
 */
public class NotTransformedException extends RuntimeException
{
  public NotTransformedException(String pTransformableTypeName)
  {
    super("The component " + pTransformableTypeName + " should have been transformed to use this operation!");
  }
}
