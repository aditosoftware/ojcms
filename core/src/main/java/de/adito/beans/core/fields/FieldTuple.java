package de.adito.beans.core.fields;

import de.adito.beans.core.IField;

import java.util.Objects;

/**
 * Combination of a bean field and its data value.
 *
 * @param <TYPE> the field's data type
 * @author Simon Danner, 25.02.2018
 */
public class FieldTuple<TYPE>
{
  private final IField<TYPE> field;
  private final TYPE value;

  public FieldTuple(IField<TYPE> pField, TYPE pValue)
  {
    field = pField;
    value = pValue;
  }

  /**
   * The bean field.
   */
  public IField<TYPE> getField()
  {
    return field;
  }

  /**
   * The associated data value.
   */
  public TYPE getValue()
  {
    return value;
  }

  @Override
  public String toString()
  {
    return field.getName() + " = " + value;
  }

  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;
    FieldTuple<?> that = (FieldTuple<?>) pOther;
    return field == that.field && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(field, value);
  }
}
