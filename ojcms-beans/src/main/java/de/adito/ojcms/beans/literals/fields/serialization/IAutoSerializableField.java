package de.adito.ojcms.beans.literals.fields.serialization;

import java.io.Serializable;

/**
 * Marks bean fields that already refer to a serializable value.
 *
 * @param <VALUE> the serializable value type
 * @author Simon Danner, 05.01.2020
 */
public interface IAutoSerializableField<VALUE extends Serializable> extends ISerializableField<VALUE, VALUE>
{
  @Override
  default VALUE toPersistent(VALUE pValue)
  {
    return pValue;
  }

  @Override
  default VALUE fromPersistent(VALUE pSerialValue)
  {
    return pSerialValue;
  }

  @Override
  default Class<VALUE> getSerialValueType()
  {
    return getDataType();
  }
}
