package de.adito.ojcms.sql.datasource.util;

import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableField;
import de.adito.ojcms.beans.literals.fields.util.*;
import de.adito.ojcms.sqlbuilder.definition.*;
import de.adito.ojcms.sqlbuilder.serialization.*;
import org.jetbrains.annotations.Nullable;

import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.sql.ResultSet;

/**
 * Serialization utility for bean values.
 *
 * @author Simon Danner, 19.02.2018
 */
@ApplicationScoped
public class BeanSQLSerializer extends DefaultValueSerializer
{
  @Override
  @Nullable
  public <VALUE> ISerialValue toSerial(IColumnValueTuple<VALUE> pColumnValueTuple)
  {
    if (pColumnValueTuple instanceof IBeanFieldTupleBased)
      //noinspection unchecked
      return _toSerialValue(((IBeanFieldTupleBased<VALUE>) pColumnValueTuple).getFieldValueTuple());

    return super.toSerial(pColumnValueTuple);
  }

  @Override
  @Nullable
  public <VALUE> VALUE fromSerial(IColumnIdentification<VALUE> pColumn, ResultSet pResultSet, int pIndex)
  {
    return _fromPersistent(pColumn, pResultSet, pIndex);
  }

  /**
   * Converts a bean data value to its serial format.
   *
   * @param pTuple  a tuple of bean field and associated data value
   * @param <VALUE> the data value's type
   * @return the value in its serializable format
   */
  private <VALUE, SERIAL extends Serializable> ISerialValue _toSerialValue(FieldValueTuple<VALUE> pTuple)
  {
    final IField<VALUE> field = pTuple.getField();
    final VALUE value = pTuple.getValue();

    if (value == null)
      return null;

    if (!(field instanceof ISerializableField))
      throw new BeanSerializationException(_notSerializableMessage(field, true));

    final SERIAL serialValue = ((ISerializableField<VALUE, SERIAL>) field).toPersistent(value);
    //noinspection unchecked
    final Class<SERIAL> serialType = (Class<SERIAL>) serialValue.getClass();
    return createSerialValue(serialType, serialValue);
  }

  /**
   * Converts a serial value back to the bean's data value.
   *
   * @param pColumn    the database column the bean field is associated with
   * @param pResultSet the result set to retrieve the serial value from
   * @param pIndex     the index of the serial value within the result set
   * @param <VALUE>    the data type of the bean field
   * @return the converted data value
   */
  @SuppressWarnings("unchecked")
  private <VALUE, SERIAL extends Serializable> VALUE _fromPersistent(IColumnIdentification<?> pColumn, ResultSet pResultSet, int pIndex)
  {
    final SERIAL serialValue = (SERIAL) super.fromSerial(pColumn, pResultSet, pIndex);

    if (!(pColumn instanceof IBeanFieldTupleBased))
      return (VALUE) serialValue;

    final IField<VALUE> field = ((IBeanFieldTupleBased<VALUE>) pColumn).getFieldValueTuple().getField();
    if (!(field instanceof ISerializableField))
      throw new BeanSerializationException(_notSerializableMessage(field, false));

    return ((ISerializableField<VALUE, SERIAL>) field).fromPersistent(serialValue);
  }

  /**
   * Generates an error message for non convertable values.
   *
   * @param pField    the bean field the value belongs to
   * @param pToSerial <tt>true</tt>, if the conversion is from data value to persistent value
   * @param <VALUE>   the generic data type
   * @return the error message
   */
  private static <VALUE> String _notSerializableMessage(IField<VALUE> pField, boolean pToSerial)
  {
    return "Unable to " + (pToSerial ? "persist" : "read") + " the value of the bean field " + pField.getName() +
        " with type " + pField.getDataType() + "! The field must either be a reference or serializable field!";
  }
}
