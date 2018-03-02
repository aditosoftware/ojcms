package de.adito.beans.core.fields;

import de.adito.beans.core.IField;

import java.util.Objects;

/**
 * Extension for a {@link IField} to support serializable field values.
 *
 * @author Simon Danner, 19.02.2018
 */
public interface ISerializableField<TYPE> extends IField<TYPE>
{
  /**
   * The value in a persistent string format.
   *
   * @param pValue the field's current value
   * @return the value in serializable string format
   */
  default String toPersistent(TYPE pValue)
  {
    return Objects.toString(pValue);
  }

  /**
   * The original field's value from a persistent string format.
   *
   * @param pSerialString the serialized string
   * @return the original field value
   */
  TYPE fromPersistent(String pSerialString);
}
