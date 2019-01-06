package de.adito.ojcms.beans.literals.fields.util;

import de.adito.ojcms.beans.literals.fields.IField;

import java.util.Objects;

/**
 * Combination of a bean field and its data value.
 *
 * @param <VALUE> the field's data type
 * @author Simon Danner, 25.02.2018
 */
public class FieldValueTuple<VALUE>
{
  private final IField<VALUE> field;
  private final VALUE value;

  public FieldValueTuple(IField<VALUE> pField, VALUE pValue)
  {
    field = pField;
    value = pValue;
  }

  /**
   * The bean field.
   *
   * @return the bean field
   */
  public IField<VALUE> getField()
  {
    return field;
  }

  /**
   * The associated data value.
   *
   * @return the value
   */
  public VALUE getValue()
  {
    return value;
  }

  /**
   * Determines, if the value of this tuple is the field's default value.
   *
   * @return <tt>true</tt>, if it is the default value
   */
  public boolean isDefaultValue()
  {
    return Objects.equals(getField().getDefaultValue(), getValue());
  }

  /**
   * Determines, if the value of this tuple is the field's initial value.
   *
   * @return <tt>true</tt>, if it is the initial value
   */
  public boolean isInitialValue()
  {
    return Objects.equals(getField().getInitialValue(), getValue());
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
    FieldValueTuple<?> that = (FieldValueTuple<?>) pOther;
    return field == that.field && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(field, value);
  }
}
