package de.adito.beans.core.util.exceptions;

/**
 * Exception, welche auftritt, wenn die Transformation einer transformierbaren Komponente bereits stattfand,
 * aber eine Aktion darauf ausgelegt ist, vor einer Transformation stattzufinden.
 *
 * @author s.danner, 18.07.2017
 */
public class AlreadyTransformedException extends RuntimeException
{
  public AlreadyTransformedException(String pTransformableTypeName)
  {
    super(pTransformableTypeName);
  }
}
