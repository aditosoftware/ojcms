package de.adito.beans.core.util.beancopy;

import de.adito.beans.core.IField;

import java.util.function.Function;

/**
 * A wrapper class for creating custom bean field value copies.
 *
 * @author Simon Danner, 14.04.2018
 */
public class CustomFieldCopy<TYPE>
{
  private final IField<TYPE> field;
  private final Function<TYPE, TYPE> copyCreator;

  /**
   * Creates a custom field copy information wrapper.
   *
   * @param pField       the bean field to create the value's copy from
   * @param pCopyCreator a function to create a copy of the value
   */
  public CustomFieldCopy(IField<TYPE> pField, Function<TYPE, TYPE> pCopyCreator)
  {
    field = pField;
    copyCreator = pCopyCreator;
  }

  /**
   * The bean field to create the value copy from.
   */
  public IField<TYPE> getField()
  {
    return field;
  }

  /**
   * The function to create the value copy.
   */
  public Function<TYPE, TYPE> getCopyCreator()
  {
    return copyCreator;
  }
}
