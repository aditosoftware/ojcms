package de.adito.beans.core.util.exceptions;

/**
 * Exception, welche auftritt, wenn die Transformation einer transformierbaren Komponente noch nicht stattfand,
 * aber bereits auf Aktionen zurückgegriffen wird, welche eine solche benötigen.
 *
 * @author s.danner, 18.07.2017
 */
public class NotTransformedException extends RuntimeException
{
  public NotTransformedException(String pTransformableTypeName)
  {
    super(pTransformableTypeName);
  }
}
