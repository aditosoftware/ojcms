package de.adito.beans.core.fields.serialization;

import java.util.Objects;

/**
 * Serializable bean field using {@link Object#toString()} to persist field values.
 *
 * @param <VALUE> the value type of the bean field
 * @author Simon Danner, 07.12.2018
 */
public interface ISerializableFieldToString<VALUE> extends ISerializableField<VALUE>
{
  @Override
  default String toPersistent(VALUE pValue)
  {
    return Objects.toString(pValue);
  }
}