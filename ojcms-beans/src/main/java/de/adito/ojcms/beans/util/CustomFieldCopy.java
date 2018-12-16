package de.adito.ojcms.beans.util;

import de.adito.ojcms.beans.fields.IField;

import java.util.function.Function;

/**
 * Information for creating custom bean field value copies.
 *
 * @param <VALUE> the data type of the bean field to copy values from
 * @author Simon Danner, 14.04.2018
 */
public class CustomFieldCopy<VALUE>
{
  private final IField<VALUE> field;
  private final Function<VALUE, VALUE> copyCreator;

  /**
   * Creates a custom field copy information.
   *
   * @param pField       the bean field to create the value's copy from
   * @param pCopyCreator a function to create a copy of the value
   */
  public CustomFieldCopy(IField<VALUE> pField, Function<VALUE, VALUE> pCopyCreator)
  {
    field = pField;
    copyCreator = pCopyCreator;
  }

  /**
   * The bean field to create the value copy from.
   *
   * @return the bean field to create the copy for
   */
  public IField<VALUE> getField()
  {
    return field;
  }

  /**
   * The function to create the value copy.
   *
   * @return a function creating the copied value
   */
  public Function<VALUE, VALUE> getCopyCreator()
  {
    return copyCreator;
  }
}
