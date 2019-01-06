package de.adito.ojcms.beans.exceptions.bean;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.literals.fields.IField;

import java.util.List;

/**
 * Exception for invalid bean field chains to retrieve deep beans or values of a bean.
 * See {@link IBean#resolveDeepBean(List)} or {@link IBean#resolveDeepValue(IField, List)}.
 *
 * @author Simon Danner, 23.12.2018
 */
public class InvalidChainException extends OJRuntimeException
{
  /**
   * Creates a new invalid chain exception for a parent bean that doesn't contain a specific field to proceed within the chain.
   *
   * @param pParentBean      the parent bean not containing the specific field
   * @param pNotPresentField the missing bean field
   */
  public InvalidChainException(IBean<?> pParentBean, IField<?> pNotPresentField)
  {
    this("The parent bean " + pParentBean + " does not contain a field " + pNotPresentField.getName() + "!");
  }

  /**
   * Creates a new invalid chain exception for a non bean reference field.
   *
   * @param pNoBeanField the bean field of a bad type (cannot create a reference)
   */
  public InvalidChainException(IField<?> pNoBeanField)
  {
    this("It can only contain bean reference fields. " + pNoBeanField.getName() + " is of data type " + pNoBeanField.getDataType().getName());
  }

  /**
   * Internal exception constructor adding a prefix to the detail message.
   *
   * @param pDetailMessage the detail message to prefix
   */
  private InvalidChainException(String pDetailMessage)
  {
    super("Invalid chain! " + pDetailMessage);
  }
}
