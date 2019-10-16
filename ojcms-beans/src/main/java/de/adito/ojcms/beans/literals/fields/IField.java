package de.adito.ojcms.beans.literals.fields;

import de.adito.ojcms.beans.exceptions.OJRuntimeException;
import de.adito.ojcms.beans.exceptions.copy.BeanCopyNotSupportedException;
import de.adito.ojcms.beans.literals.IMemberLiteral;
import de.adito.ojcms.beans.literals.fields.util.*;
import de.adito.ojcms.beans.util.*;

import java.util.*;
import java.util.function.*;

/**
 * A field of a bean that is based on an inner data type.
 * It's just a wrapper for the actual data that holds additional meta information of the field.
 *
 * @param <VALUE> the inner data type of this bean field
 * @author Simon Danner, 23.08.2016
 */
public interface IField<VALUE> extends IMemberLiteral
{
  /**
   * The bean field type for a specific inner data type.
   * The data type of the field may be different than the given data type because of possibly present converters.
   *
   * @param pDataType the data type to find the bean field type for
   * @return the bean field type
   * @throws OJRuntimeException if the bean field type could not be determined
   */
  static Class<IField<?>> getFieldTypeFromDataType(Class<?> pDataType)
  {
    return findFieldTypeFromDataType(pDataType)
        .orElseThrow(() -> new OJRuntimeException("There is no bean field for this data type: " + pDataType.getSimpleName()));
  }

  /**
   * Tries to find the bean field type for a specific inner data type.
   * The data type of the field may be different than the given data type because of possibly present converters.
   *
   * @param pDataType the data type to find the bean field type for
   * @return an optional bean field type
   */
  static Optional<Class<IField<?>>> findFieldTypeFromDataType(Class<?> pDataType)
  {
    return FieldJavaTypes.findFieldTypeFromDataType(pDataType);
  }

  /**
   * The inner data type of this field.
   *
   * @return the data type of this field
   */
  Class<VALUE> getDataType();

  /**
   * A default value for this field. By default it is the initial value.
   * A default value may be used in special scenarios and defined for such cases exclusively.
   * Do not mix this up with {@link IField#getInitialValue()}, which returns the initial value
   * for a certain data type (like 'false' for boolean)
   *
   * @return the default value for this field
   */
  default VALUE getDefaultValue()
  {
    return getInitialValue();
  }

  /**
   * An initial value for this field's data type. (e.g. 'false' for boolean)
   * By default it is null for reference types.
   *
   * @return the initial value for this field
   */
  default VALUE getInitialValue()
  {
    return null;
  }

  /**
   * A string representation of the data value of this bean field.
   * By default it is the result of {@link Object#toString()} of the value.
   *
   * @param pValue             the field's actual data value
   * @param pClientSessionInfo information of a client (time zone etc.)
   * @return the string representation of this field
   */
  default String display(VALUE pValue, IClientInfo pClientSessionInfo)
  {
    return Objects.toString(pValue);
  }

  /**
   * Creates a copy of the value of this field.
   * Optionally {@link CustomFieldCopy} information may be provided to copy certain field values in a special way.
   * If the copied value is deep or shallow depends on the {@link ECopyMode}.
   *
   * @param pValue             the value to create the copy from
   * @param pMode              the copy mode (deep or shallow)
   * @param pCustomFieldCopies a collection of custom copy mechanisms for specific bean fields
   * @return a copy of the field value
   * @throws BeanCopyNotSupportedException if, it is not possible to create a copy of the specified data type
   */
  default VALUE copyValue(VALUE pValue, ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies) throws BeanCopyNotSupportedException
  {
    throw new BeanCopyNotSupportedException(this);
  }

  /**
   * A converter for a specific source type to convert its value to the bean field's data type.
   * Converters for certain data types have to be registered by the actual field implementation.
   * Hence the result type of this method is {@link Optional}, because converters may not be available.
   *
   * @param pSourceType the type of the value to convert to the field's data type
   * @param <SOURCE>    the generic source type of the value to convert
   * @return an optional converter (may not be registered)
   */
  <SOURCE> Optional<Function<SOURCE, VALUE>> getToConverter(Class<SOURCE> pSourceType);

  /**
   * A converter to convert the value of this bean field to a specific target data type.
   * Converters for certain data types have to be registered by the actual field implementation.
   * Hence the result type of this method is {@link Optional}, because converters may not be available.
   *
   * @param pTargetType the type of the value that will be converted from the field's data type value
   * @return an optional converter (may not be registered)
   */
  <TARGET> Optional<Function<VALUE, TARGET>> getFromConverter(Class<TARGET> pTargetType);

  /**
   * Determines if the field's value is final.
   *
   * @return <tt>true</tt> if the field is final
   */
  boolean isValueFinal();

  /**
   * Determines, if this field is an identifier.
   *
   * @return <tt>true</tt>, if this field is an identifier
   */
  boolean isIdentifier();

  /**
   * Determines, if this field is optional.
   * If the field is optional, a related condition will define, when the field will be active.
   *
   * @return <tt>true</tt>, if this field is optional and is only present under a certain condition
   */
  boolean isOptional();

  /**
   * Determines if the field's value must never be null.
   *
   * @return <tt>true</tt> if the value must never be null
   */
  boolean mustNeverBeNull();

  /**
   * Determines, if this field is marked as detail.
   *
   * @return <tt>true</tt>, if this field may be treated as detail
   */
  boolean isDetail();

  /**
   * Creates a new empty tuple (value = null) from this bean field.
   *
   * @return an empty field tuple
   */
  default FieldValueTuple<VALUE> emptyTuple()
  {
    return newTuple(null);
  }

  /**
   * Creates a new field tuple from this bean field.
   *
   * @param pValue the value for the tuple
   * @return a new field tuple
   */
  default FieldValueTuple<VALUE> newTuple(VALUE pValue)
  {
    return new FieldValueTuple<>(this, pValue);
  }

  /**
   * Creates a new empty tuple (value = null) from this bean field.
   * This method will create a untyped tuple.
   *
   * @return a empty field tuple
   */
  default FieldValueTuple<?> emptyUntypedTuple()
  {
    return newUntypedTuple(null);
  }

  /**
   * Creates a new field tuple from this bean field.
   * This method will create a untyped tuple.
   *
   * @param pValue the value for the tuple
   * @return a new field tuple
   */
  default FieldValueTuple<?> newUntypedTuple(Object pValue)
  {
    if (pValue != null && !getDataType().isAssignableFrom(pValue.getClass()))
      throw new OJRuntimeException("type-mismatch for field value tuple:" +
                                       " field type: " + getDataType().getName() + " value type: " + pValue.getClass().getName());
    //noinspection unchecked
    return new FieldValueTuple(this, pValue);
  }

  /**
   * Creates a custom field copy creator from this bean field.
   *
   * @param pCopyCreator a function that will create a copy of the field's value
   * @return the copy creator wrapper
   * @see CustomFieldCopy
   */
  default CustomFieldCopy<VALUE> customFieldCopy(UnaryOperator<VALUE> pCopyCreator)
  {
    return new CustomFieldCopy<>(this, pCopyCreator);
  }
}
