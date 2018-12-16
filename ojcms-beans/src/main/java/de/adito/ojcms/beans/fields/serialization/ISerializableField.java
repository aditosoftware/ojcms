package de.adito.ojcms.beans.fields.serialization;

import de.adito.ojcms.beans.fields.IField;

/**
 * Extension for a {@link IField} to support serializable field values.
 *
 * @param <VALUE> the data type of the bean field
 * @author Simon Danner, 19.02.2018
 */
public interface ISerializableField<VALUE> extends IField<VALUE>
{
  /**
   * The value in a persistent string format.
   *
   * @param pValue the field's current value
   * @return the value in serializable string format
   */
  String toPersistent(VALUE pValue);

  /**
   * The original field's value from a persistent string format.
   *
   * @param pSerialString the serialized string format
   * @return the original field value
   */
  VALUE fromPersistent(String pSerialString);
}
