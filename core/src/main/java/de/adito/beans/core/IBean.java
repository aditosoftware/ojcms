package de.adito.beans.core;

import de.adito.beans.core.annotations.Identifier;
import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.mappers.*;
import de.adito.beans.core.references.IHierarchicalBeanStructure;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.IBeanFieldPredicate;
import de.adito.beans.core.util.beancopy.*;
import de.adito.beans.core.util.exceptions.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * The functional wrapper interface of a bean.
 * A bean is separated in this wrapper and an encapsulated data core.
 * This interface provides the whole functionality via default methods.
 * The default methods use the only non-default method {@link IEncapsulatedHolder#getEncapsulated()} to get access to the data core.
 * This method may be called 'virtual field', because it gives access to an imaginary field that holds the data core.
 * This means, you only have to give a reference to any bean core to get a completed bean, if this interface is used.
 *
 * This interface is implemented by the default bean class {@link Bean}, which is used to create the application's beans.
 * But it may also be used for any other class that should be treated as bean.
 * Furthermore you are able to extend this interface through special methods for your use case.
 * Through the use of an interface it is possible to extend the bean type to a class that already extends another class.
 * This might seem like a solution to the not available multi inheritance in Java, but only the base interface type
 * is transferred to the extending class. Methods and static field definitions stay at the concrete bean types.
 *
 * @param <BEAN> the concrete type of the bean that is implementing the interface
 * @author Simon Danner, 23.08.2016
 */
public interface IBean<BEAN extends IBean<BEAN>> extends IEncapsulatedHolder<IBeanEncapsulated<BEAN>>
{
  /**
   * The value for a bean field.
   * This method can only be called if the field has no private access modifier {@link de.adito.beans.core.annotations.Private}.
   *
   * @param pField the bean field
   * @param <TYPE> the field's data type
   * @return the value for the bean field
   */
  default <TYPE> TYPE getValue(IField<TYPE> pField)
  {
    assert getEncapsulated() != null;
    if (!getEncapsulated().containsField((Objects.requireNonNull(pField))))
      throw new BeanFieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    return getEncapsulated().getValue(pField);
  }

  /**
   * The value for a bean field if not null.
   * Otherwise the field's default value will be returned.
   * This method can only be called if the field has no private access modifier {@link de.adito.beans.core.annotations.Private}.
   *
   * @param pField the bean field
   * @param <TYPE> the field's data type
   * @return the value for the bean field or the field's default value if null
   */
  default <TYPE> TYPE getValueOrDefault(IField<TYPE> pField)
  {
    return Optional.ofNullable(getValue(pField)).orElse(pField.getDefaultValue());
  }

  /**
   * The value for a bean field.
   * Here it's possible to define a type to which the value should be transformed before it is returned.
   * The associated field must be able to provide a matching converter.
   * This method can only be called if the field has no private access modifier {@link de.adito.beans.core.annotations.Private}.
   *
   * @param pField       the bean field
   * @param pConvertType the type to which the value should be transformed
   * @param <TYPE>       the field's data type
   * @param <SOURCE>     the generic type to which should be transformed
   * @return the converted value for the bean field
   */
  default <TYPE, SOURCE> SOURCE getValueConverted(IField<TYPE> pField, Class<SOURCE> pConvertType)
  {
    TYPE actualValue = getValue(pField);
    if (actualValue == null || pConvertType.isAssignableFrom(actualValue.getClass()))
      //noinspection unchecked
      return (SOURCE) actualValue;
    return pField.getFromConverter(pConvertType)
        .orElseThrow(() -> new RuntimeException("The field " + pField.getName() + " is not able to convert to " + pConvertType.getSimpleName()))
        .apply(actualValue);
  }

  /**
   * Sets a value for a bean field.
   * If the new value is different from the old value, all registered listeners will be informed.
   * This method can only be called if the field has no private access modifier {@link de.adito.beans.core.annotations.Private}.
   *
   * @param pField the bean field for which the value should be set
   * @param pValue the new value
   * @param <TYPE> the field's data type
   */
  default <TYPE> void setValue(IField<TYPE> pField, TYPE pValue)
  {
    assert getEncapsulated() != null;
    if (!getEncapsulated().containsField(Objects.requireNonNull(pField)))
      throw new BeanFieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    //noinspection unchecked
    BeanListenerUtil.setValueAndFire((BEAN) this, pField, pValue);
  }

  /**
   * Sets a value for a bean field.
   * Here the value may differ from the field's data type.
   * In this case, a converter, provided by the field, will be used to convert the value beforehand.
   * If there is no matching converter, a runtime exception will be thrown.
   * If the new value is different from the old value, all registered listeners will be informed.
   * This method can only be called if the field has no private access modifier {@link de.adito.beans.core.annotations.Private}.
   *
   * @param pField          the bean field for which the value should be set
   * @param pValueToConvert the new value that possibly has to be transformed beforehand
   * @param <TYPE>          he field's data type
   * @param <SOURCE>        the value's type before its conversion
   */
  @SuppressWarnings("unchecked")
  default <TYPE, SOURCE> void setValueConverted(IField<TYPE> pField, SOURCE pValueToConvert)
  {
    TYPE convertedValue = null;
    if (pValueToConvert != null)
    {
      Class<SOURCE> sourceType = (Class<SOURCE>) pValueToConvert.getClass();
      convertedValue = pField.getType().isAssignableFrom(sourceType) ? (TYPE) pValueToConvert :
          pField.getToConverter(sourceType)
              .orElseThrow(() -> new RuntimeException("The field " + pField.getName() + " cannot convert to " + sourceType.getSimpleName()))
              .apply(pValueToConvert);
    }
    setValue(pField, convertedValue);
  }

  /**
   * Clears the values of all public field's of this bean.
   * The values are 'null' afterwards.
   */
  default void clear()
  {
    streamFields()
        .filter(pField -> !pField.isPrivate())
        .forEach(pField -> setValue(pField, null));
  }

  /**
   * An interface to determine, if an optional bean field is active at a certain time.
   *
   * @see IBeanFieldActivePredicate
   */
  default IBeanFieldActivePredicate<BEAN> getFieldActiveSupplier()
  {
    //noinspection unchecked
    return () -> (BEAN) this;
  }

  /**
   * Determines, if this bean has a certain field.
   * Ignores private fields.
   *
   * @param pField the bean field to check
   * @return <tt>true</tt> if the field is present
   */
  default boolean hasField(IField<?> pField)
  {
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    assert getEncapsulated() != null;
    return getEncapsulated().containsField(pField);
  }

  /**
   * The amount of fields of this bean.
   * Ignores private fields.
   *
   * @return the public field count
   */
  default int getFieldCount()
  {
    assert getEncapsulated() != null;
    return (int) streamFields().count();
  }

  /**
   * The index of a bean field.
   * Generally the index depends on the order of the defined fields.
   * Ignores private fields.
   *
   * @param pField the bean field
   * @param <TYPE> the field's data type
   * @return the index of the field, or -1 if not present
   */
  default <TYPE> int getFieldIndex(IField<TYPE> pField)
  {
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    return streamFields()
        .collect(Collectors.toList())
        .indexOf(pField);
  }

  /**
   * A hierarchical structure of references to this bean.
   * The structure contains direct and deep parent-references to this bean.
   * A reference occurs through a bean or container field. (Default Java references are ignored)
   *
   * @return an interface to retrieve information about the hierarchical reference structure
   * @see IHierarchicalBeanStructure
   */
  default IHierarchicalBeanStructure getHierarchicalStructure()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getHierarchicalStructure();
  }

  /**
   * Registers a weak change listener.
   *
   * @param pListener a listener that gets informed about value changes of this bean
   */
  default void listenWeak(IBeanChangeListener<BEAN> pListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addListener(Objects.requireNonNull(pListener));
  }

  /**
   * Unregisters a change listener.
   *
   * @param pListener the listener to deregister
   */
  default void unlisten(IBeanChangeListener<BEAN> pListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().removeListener(Objects.requireNonNull(pListener));
  }

  /**
   * Creates a copy of this bean.
   * This method expects an existing default constructor for this concrete bean type.
   * If the copy should include deep fields, all deep beans are supposed to have default constructors as well.
   * If it is not possible to provide a default constructor, you may use the other method to create bean copies.
   * It allows you to define a custom constructor call to create the new instance.
   *
   * @param pDeepCopy          <tt>true</tt>, if the copy of the bean should also include deep values
   * @param pCustomFieldCopies a collection of custom copy mechanisms for specific bean fields
   * @return a copy of this bean
   */
  default BEAN createCopy(boolean pDeepCopy, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return BeanCopyUtil.createCopy((BEAN) this, pDeepCopy, pCustomFieldCopies);
  }

  /**
   * Creates a copy of this bean.
   * This method should be used, if there's no default constructor to create a new instance automatically.
   * Otherwise use the other method to create the copy, where you are not supposed to define a custom constructor call.
   * If the copy should be deep, all deep bean values are supposed to have a default constructors.
   *
   * @param pDeepCopy              <tt>true</tt>, if the copy of the bean should also include deep values
   * @param pCustomConstructorCall a custom constructor call defined as function (the input is the existing bean, the function should create the copy)
   * @param pCustomFieldCopies     a collection of custom copy mechanisms for specific bean fields
   * @return a copy of this bean
   */
  default BEAN createCopy(boolean pDeepCopy, Function<BEAN, BEAN> pCustomConstructorCall, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return BeanCopyUtil.createCopy((BEAN) this, pDeepCopy, pCustomConstructorCall, pCustomFieldCopies);
  }

  /**
   * The statistic data for a certain bean field.
   * May be null if not present.
   *
   * @param pField the bean field
   * @param <TYPE> the data type of the field
   * @return the statistic data, or null if not existing
   */
  @Nullable
  default <TYPE> IStatisticData<TYPE> getStatisticData(IField<TYPE> pField)
  {
    if (!hasField(Objects.requireNonNull(pField)))
      throw new BeanFieldDoesNotExistException(this, pField);
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().getStatisticData().get(pField);
  }

  /**
   * All field tuples marked as identifiers within this bean.
   * Identifiers could be used to find related beans in two containers. (comparable to primary key columns in DB-systems)
   *
   * @return a set of field tuples
   */
  default Set<FieldTuple<?>> getIdentifiers()
  {
    return stream()
        .filter(pTuple -> pTuple.getField().hasAnnotation(Identifier.class))
        .collect(Collectors.toSet());
  }

  /**
   * Searches a bean field by its name.
   *
   * @param pName the name to search for
   * @return a Optional that may contain the bean field
   */
  default Optional<IField<?>> findFieldByName(String pName)
  {
    return streamFields()
        .filter(pField -> pField.getName().equals(pName))
        .findAny();
  }

  /**
   * Adds a field filter to this data core.
   * So fields with their associated values may be excluded for a certain time.
   *
   * If a field is excluded, the bean behaves like the field isn't present at all.
   * Hence methods could throw runtime exceptions, when a field is inactive at a certain moment.
   * This could be compared to optional field definitions ({@link de.adito.beans.core.annotations.OptionalField}.
   * But in contrast to them field filters may be used temporary.
   *
   * @param pPredicate the predicate to define the excluded fields
   */
  default void addFieldFilter(IBeanFieldPredicate pPredicate)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addFieldFilter(pPredicate);
  }

  /**
   * Removes a field filter from this data core.
   *
   * @param pPredicate the predicate/filter to remove
   */
  default void removeFieldFilter(IBeanFieldPredicate pPredicate)
  {
    assert getEncapsulated() != null;
    getEncapsulated().removeFieldFilter(pPredicate);
  }

  /**
   * Clears all field filters.
   */
  default void clearFieldFilters()
  {
    assert getEncapsulated() != null;
    getEncapsulated().clearFieldFilters();
  }

  /**
   * Adds a temporary data mapper to this data core.
   * A data mapper applies to the tuple-stream of this bean.
   * It may be used to present values in specific ways temporary.
   *
   * @param pDataMapper the data mapper
   */
  default void addDataMapper(IBeanFlatDataMapper pDataMapper)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addDataMapper(pDataMapper);
  }

  /**
   * Adds a temporary data mapper, which only applies to a single field, to this data core.
   * A data mapper applies to the tuple-stream of this bean.
   * It may be used to present values in specific ways temporary.
   *
   * @param pDataMapper the data mapper
   */
  default <TYPE> void addDataMapperForField(IField<TYPE> pField, ISingleFieldFlatDataMapper<TYPE> pDataMapper)
  {
    assert getEncapsulated() != null;
    getEncapsulated().addDataMapperForField(pField, pDataMapper);
  }

  /**
   * Removes a specific data mappers from this data core.
   * The method can be used for normal mappers and single field mappers.
   *
   * @param pDataMapper the data mapper to remove
   * @return <tt>true</tt>, if the mapper has been removed successfully
   */
  default boolean removeDataMapper(IBeanFlatDataMapper pDataMapper)
  {
    assert getEncapsulated() != null;
    return getEncapsulated().removeDataMapper(pDataMapper);
  }

  /**
   * Clears all data mappers (normal and single) from this data core.
   */
  default void clearDataMappers()
  {
    assert getEncapsulated() != null;
    getEncapsulated().clearDataMappers();
  }

  /**
   * A stream containing all fields of this bean.
   * Ignores private fields.
   *
   * @return a stream of bean fields
   */
  default Stream<IField<?>> streamFields()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().streamFields()
        .filter(pField -> !pField.isPrivate())
        .filter(pField -> getFieldActiveSupplier().isOptionalActive(pField));
  }

  /**
   * This bean as stream.
   * It contains all field value tuples.
   * Ignores private fields.
   *
   * @return a stream of field tuples
   */
  default Stream<FieldTuple<?>> stream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().stream()
        .filter(pFieldTuple -> !pFieldTuple.getField().isPrivate())
        .filter(pFieldTuple -> getFieldActiveSupplier().isOptionalActive(pFieldTuple.getField()));
  }
}
