package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.IField;
import de.adito.beans.core.fields.*;
import de.adito.beans.core.references.IHierarchicalField;

/**
 * Serialization utility for bean values.
 *
 * @author Simon Danner, 19.02.2018
 */
public final class SQLSerializer
{
  private static final String REF_FIELD = IHierarchicalField.class.getSimpleName();
  private static final String SERIALIZABLE_FIELD = ISerializableField.class.getSimpleName();

  private SQLSerializer()
  {
  }

  /**
   * Converts a bean data value to its serializable format.
   *
   * @param pTuple a tuple of bean field and associated data value
   * @param <TYPE> the data value's type
   * @return the value in its serializable format
   */
  public static <TYPE> String toPersistent(FieldTuple<TYPE> pTuple)
  {
    IField<TYPE> field = pTuple.getField();

    if (field instanceof ISerializableField)
      return ((ISerializableField<TYPE>) field).toPersistent(pTuple.getValue());

    if (field instanceof IHierarchicalField)
      return null;

    throw new RuntimeException(_notSerializableMessage(field, true));
  }

  /**
   * Converts a persistent value back to the bean's data value.
   *
   * @param pField        the bean field the value belongs to
   * @param pSerialString the persistent value
   * @param <TYPE>        the data type
   * @return the converted data value
   */
  public static <TYPE> TYPE fromPersistent(IField<TYPE> pField, String pSerialString)
  {
    if (pField instanceof ISerializableField)
      return ((ISerializableField<TYPE>) pField).fromPersistent(pSerialString);

    if (pField instanceof IHierarchicalField)
      return null;

    throw new RuntimeException(_notSerializableMessage(pField, false));
  }

  /**
   * Generates a error message for non convertable values.
   *
   * @param pField    the bean field the value belongs to
   * @param pToSerial <tt>true</tt>, if the conversion is from data value to persistent value
   * @param <TYPE>    the generic data type
   * @return the error message
   */
  private static <TYPE> String _notSerializableMessage(IField<TYPE> pField, boolean pToSerial)
  {
    return "Unable to " + (pToSerial ? "persist" : "read") + " the value of the bean field " + pField.getName() +
        " with type " + pField.getType() + "! The field must either be a " + REF_FIELD + " or a " + SERIALIZABLE_FIELD + "!";
  }
}
