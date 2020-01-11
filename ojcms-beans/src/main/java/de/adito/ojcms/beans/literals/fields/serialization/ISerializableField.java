package de.adito.ojcms.beans.literals.fields.serialization;

import de.adito.ojcms.beans.literals.fields.IField;

import java.io.Serializable;

/**
 * Extension for a {@link IField} to support serializable field values.
 *
 * @param <VALUE> the data type of the bean field
 * @author Simon Danner, 19.02.2018
 */
public interface ISerializableField<VALUE, SERIAL extends Serializable> extends IField<VALUE>
{
  /**
   * The value in a persistent format.
   *
   * @param pValue the field's current value
   * @return the value in serializable format
   */
  SERIAL toPersistent(VALUE pValue);

  /**
   * The original field's value from a persistent format.
   *
   * @param pSerialValue the serial format
   * @return the original field value
   */
  VALUE fromPersistent(SERIAL pSerialValue);
}
