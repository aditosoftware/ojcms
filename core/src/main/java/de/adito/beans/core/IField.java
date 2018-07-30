package de.adito.beans.core;

import de.adito.beans.core.fields.*;
import de.adito.beans.core.util.IClientInfo;
import de.adito.beans.core.util.beancopy.CustomFieldCopy;
import de.adito.beans.core.util.exceptions.BeanCopyUnsupportedException;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * Describes a field of a bean.
 * It's based on an inner data type.
 * A bean field is just a wrapper for the actual data, that contains additional information.
 *
 * @param <TYPE> the inner data type of this bean field
 * @author Simon Danner, 23.08.2016
 */
public interface IField<TYPE>
{
  /**
   * The inner data type of this field.
   */
  Class<TYPE> getType();

  /**
   * The name of this field.
   */
  String getName();

  /**
   * A default value for this field.
   * By default it is null.
   *
   * @return the default value for this field
   */
  default TYPE getDefaultValue()
  {
    return null;
  }

  /**
   * An initial value for this field.
   * By default it is null for reference types.
   *
   * @return the initial value for this field
   */
  default TYPE getInitialValue()
  {
    return null;
  }

  /**
   * A string representation of the value of this bean field.
   * By default it is the result of {@link Object#toString()} of the value.
   *
   * @param pValue             the field's actual data value
   * @param pClientSessionInfo information of a client (time zone etc.)
   * @return the string representation of this field
   */
  default String display(TYPE pValue, IClientInfo pClientSessionInfo)
  {
    return Objects.toString(pValue);
  }

  /**
   * Creates a copy of the value of this field.
   *
   * @param pValue the value to create the copy from
   * @return a copy of the field value
   * @throws UnsupportedOperationException if, it is not possible to create a copy
   */
  default TYPE copyValue(TYPE pValue, CustomFieldCopy<?>... pCustomFieldCopies) throws BeanCopyUnsupportedException
  {
    throw new BeanCopyUnsupportedException(this);
  }

  /**
   * Returns a converter to convert a certain value type to the field's data type.
   * A converter for a type has to be registered by the field. Hence the result type of this method is optional.
   *
   * @param pSourceType the type of the value to convert to the field's data type
   * @return a optional converter (may not be registered)
   */
  <SOURCE> Optional<Function<SOURCE, TYPE>> getToConverter(Class<SOURCE> pSourceType);

  /**
   * Returns a converter to convert the field's data type to a certain source type.
   * A converter for a type has to be registered by the field. Hence the result type of this method is optional.
   *
   * @param pSourceType the type of the value that will be converted from the field's data type value
   * @return a optional converter (may not be registered)
   */
  <SOURCE> Optional<Function<TYPE, SOURCE>> getFromConverter(Class<SOURCE> pSourceType);

  /**
   * The annotation of this field for a certain annotation type. (null, if not present)
   *
   * @param pType the annotation's type
   * @return the annotation object for the given type, or null if not present
   */
  @Nullable <ANNOTATION extends Annotation> ANNOTATION getAnnotation(Class<ANNOTATION> pType);

  /**
   * Determines, if this field has a certain annotation.
   *
   * @param pType the annotation's type
   * @return <tt>true</tt>, if the field has the annotation
   */
  boolean hasAnnotation(Class<? extends Annotation> pType);

  /**
   * All annotations of this field.
   *
   * @return a collection of annotations
   */
  Collection<Annotation> getAnnotations();

  /**
   * An additional information for this field.
   *
   * @param pIdentifier the identifier of the information
   * @return the additional information
   */
  @Nullable <INFO> INFO getAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier);

  /**
   * Adds any additional information to this field.
   *
   * @param pIdentifier the identifier of this information
   * @param pValue      any information
   * @see IAdditionalFieldInfo
   */
  <INFO> void addAdditionalInformation(IAdditionalFieldInfo<INFO> pIdentifier, INFO pValue);

  /**
   * Determines, if this field has private access only.
   */
  boolean isPrivate();

  /**
   * Determines, if this field is an identifier.
   */
  boolean isIdentifier();

  /**
   * Determines, if this field is optional.
   * If the field is optional, a related condition will define, when the field will be active.
   */
  boolean isOptional();

  /**
   * Determines, if this field is marked as detail.
   */
  boolean isDetail();

  /**
   * Creates a new empty tuple (value = null) from this bean field.
   *
   * @return a empty field tuple
   */
  default FieldTuple<TYPE> emptyTuple()
  {
    return newTuple(null);
  }

  /**
   * Creates a new field tuple from this bean field.
   *
   * @param pValue the value for the tuple
   * @return a new field tuple
   */
  default FieldTuple<TYPE> newTuple(TYPE pValue)
  {
    return new FieldTuple<>(this, pValue);
  }

  /**
   * Creates a new empty tuple (value = null) from this bean field.
   * This method will create a untyped tuple.
   *
   * @return a empty field tuple
   */
  default FieldTuple<?> emptyUntypedTuple()
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
  default FieldTuple<?> newUntypedTuple(Object pValue)
  {
    if (pValue != null && !getType().isAssignableFrom(pValue.getClass()))
      throw new RuntimeException("type-mismatch: field type: " + getType().getName() + " value type: " + pValue.getClass().getName());
    //noinspection unchecked
    return new FieldTuple(this, pValue);
  }

  /**
   * Creates a custom field copy creator from this bean field.
   *
   * @param pCopyCreator a function that will create a copy of the field's value
   * @return the copy creator wrapper
   */
  default CustomFieldCopy<TYPE> customFieldCopy(Function<TYPE, TYPE> pCopyCreator)
  {
    return new CustomFieldCopy<>(this, pCopyCreator);
  }
}
