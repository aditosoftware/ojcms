package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.Identifier;
import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.beans.exceptions.*;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import de.adito.ojcms.beans.references.*;
import de.adito.ojcms.beans.statistics.IStatisticData;
import de.adito.ojcms.beans.util.*;
import de.adito.ojcms.utils.readonly.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * The functional wrapper interface of a bean.
 * A bean is separated in this wrapper and an encapsulated data core.
 * This interface provides the whole functionality via default methods.
 * The default methods use the non-default method {@link IEncapsulatedDataHolder#getEncapsulatedData()} to get access to the data core.
 * This method may be called 'virtual field', because it gives access to an imaginary field that holds the data core.
 * This means you only have to give a reference to any bean data core to get a completed bean, if this interface is used.
 *
 * This interface is implemented by the default abstract bean class {@link Bean}, which is used to create the application's beans.
 * But it may also be used for any other class that should be treated as bean.
 * Furthermore you are able to extend this interface through special methods for your use case.
 * Through the use of an interface it is possible to extend the bean type to a class that already extends another class.
 * This might seem like a solution to the not available multi inheritance in Java, but only the base interface type
 * is transferred to the extending class. Methods and static field definitions stay at the concrete bean type.
 *
 * @param <BEAN> the concrete type of the bean that is implementing the interface
 * @author Simon Danner, 23.08.2016
 */
@RequiresEncapsulatedAccess
public interface IBean<BEAN extends IBean<BEAN>>
    extends IBeanEventPublisher<FieldValueTuple<?>, BEAN, IBeanDataSource, IEncapsulatedBeanData>, IReferenceProvider
{
  /**
   * The value for a bean field.
   * This method can only be called if the field has no private access modifier {@link de.adito.ojcms.beans.annotations.Private}.
   *
   * @param pField  the bean field
   * @param <VALUE> the field's data type
   * @return the value for the bean field
   */
  default <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    assert getEncapsulatedData() != null;
    if (!getEncapsulatedData().containsField((Objects.requireNonNull(pField))))
      throw new BeanFieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    return getEncapsulatedData().getValue(pField);
  }

  /**
   * The value for a bean field if not null.
   * Otherwise the field's default value will be returned.
   * This method can only be called if the field has no private access modifier {@link de.adito.ojcms.beans.annotations.Private}.
   *
   * @param pField  the bean field
   * @param <VALUE> the field's data type
   * @return the value for the bean field or the field's default value if null
   */
  default <VALUE> VALUE getValueOrDefault(IField<VALUE> pField)
  {
    return Optional.ofNullable(getValue(pField)).orElse(pField.getDefaultValue());
  }

  /**
   * The value for a bean field.
   * Here it's possible to define a type to which the value should be transformed before it is returned.
   * The associated field must be able to provide a matching converter.
   * This method can only be called if the field has no private access modifier {@link de.adito.ojcms.beans.annotations.Private}.
   *
   * @param pField       the bean field
   * @param pConvertType the type to which the value should be converted
   * @param <VALUE>      the field's data type
   * @param <TARGET>     the generic type to convert to
   * @return the converted value for the bean field
   */
  default <VALUE, TARGET> TARGET getValueConverted(IField<VALUE> pField, Class<TARGET> pConvertType)
  {
    VALUE actualValue = getValue(pField);
    if (actualValue == null || pConvertType.isAssignableFrom(actualValue.getClass()))
      //noinspection unchecked
      return (TARGET) actualValue;
    return pField.getFromConverter(pConvertType)
        .orElseThrow(() -> new RuntimeException("The field " + pField.getName() + " is not able to convert to " + pConvertType.getSimpleName()))
        .apply(actualValue);
  }

  /**
   * Sets a value for a bean field.
   * If the new value is different from the old value, all registered listeners will be informed.
   * This method can only be called if the field has no private access modifier {@link de.adito.ojcms.beans.annotations.Private}.
   *
   * @param pField  the bean field for which the value should be set
   * @param pValue  the new value
   * @param <VALUE> the field's data type
   */
  @WriteOperation
  default <VALUE> void setValue(IField<VALUE> pField, VALUE pValue)
  {
    assert getEncapsulatedData() != null;
    if (!getEncapsulatedData().containsField(Objects.requireNonNull(pField)))
      throw new BeanFieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    //noinspection unchecked
    BeanEvents.setValueAndPropagate((BEAN) this, pField, pValue);
  }

  /**
   * Sets a value for a bean field.
   * Here the value may differ from the field's data type.
   * In this case, a converter, provided by the field, will be used to convert the value beforehand.
   * If there is no matching converter, a runtime exception will be thrown.
   * If the new value is different from the old value, all registered listeners will be informed.
   * This method can only be called if the field has no private access modifier {@link de.adito.ojcms.beans.annotations.Private}.
   *
   * @param pField          the bean field for which the value should be set
   * @param pValueToConvert the new value that possibly has to be transformed beforehand
   * @param <VALUE>         he field's data type
   * @param <SOURCE>        the value's type before its conversion
   */
  @WriteOperation
  @SuppressWarnings("unchecked")
  default <VALUE, SOURCE> void setValueConverted(IField<VALUE> pField, SOURCE pValueToConvert)
  {
    VALUE convertedValue = null;
    if (pValueToConvert != null)
    {
      Class<SOURCE> sourceType = (Class<SOURCE>) pValueToConvert.getClass();
      convertedValue = pField.getDataType().isAssignableFrom(sourceType) ? (VALUE) pValueToConvert :
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
  @WriteOperation
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
    assert getEncapsulatedData() != null;
    return getEncapsulatedData().containsField(pField);
  }

  /**
   * The amount of fields of this bean.
   * Ignores private fields.
   *
   * @return the public field count
   */
  default int getFieldCount()
  {
    assert getEncapsulatedData() != null;
    return (int) streamFields().count();
  }

  /**
   * The index of a bean field.
   * Generally the index depends on the order of the defined fields.
   * Ignores private fields.
   *
   * @param pField  the bean field
   * @param <VALUE> the field's data type
   * @return the index of the field, or -1 if not present
   */
  default <VALUE> int getFieldIndex(IField<VALUE> pField)
  {
    if (pField.isPrivate())
      throw new BeanIllegalAccessException(this, pField);
    return streamFields()
        .collect(Collectors.toList())
        .indexOf(pField);
  }

  /**
   * Creates a copy of this bean.
   * This method expects an existing default constructor for this concrete bean type.
   * If the copy should include deep fields, all deep beans are supposed to have default constructors as well.
   * If it is not possible to provide a default constructor, you may use the other method to create bean copies.
   * It allows you to define a custom constructor call to create the new instance.
   *
   * A copy will always be created with the default {@link IBeanDataSource}.
   * If a custom source should be injected use {@link IEncapsulatedDataHolder#setEncapsulatedDataSource(IDataSource)}.
   *
   * @param pMode              the copy mode
   * @param pCustomFieldCopies a collection of custom copy mechanisms for specific bean fields
   * @return a copy of this bean
   */
  default BEAN createCopy(ECopyMode pMode, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    assert getEncapsulatedData() != null;
    //noinspection unchecked
    return BeanCopies.createCopy((BEAN) this, pMode, pCustomFieldCopies);
  }

  /**
   * Creates a copy of this bean.
   * This method should be used, if there's no default constructor to create a new instance automatically.
   * Otherwise use the other method to create the copy, where you are not supposed to define a custom constructor call.
   * If the copy should be deep, all deep bean values are supposed to have a default constructors.
   *
   * @param pMode                  the copy mode
   * @param pCustomConstructorCall a custom constructor call defined as function (the input is the existing bean, the function should create the copy)
   * @param pCustomFieldCopies     a collection of custom copy mechanisms for specific bean fields
   * @return a copy of this bean
   */
  default BEAN createCopy(ECopyMode pMode, Function<BEAN, BEAN> pCustomConstructorCall, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    assert getEncapsulatedData() != null;
    //noinspection unchecked
    return BeanCopies.createCopy((BEAN) this, pMode, pCustomConstructorCall, pCustomFieldCopies);
  }

  /**
   * The statistic data for a certain bean field.
   * May be null if not present.
   *
   * @param pField  the bean field
   * @param <VALUE> the data type of the field
   * @return the statistic data, or null if not existing
   */
  @Nullable
  default <VALUE> IStatisticData<VALUE> getStatisticData(IField<VALUE> pField)
  {
    if (!hasField(Objects.requireNonNull(pField)))
      throw new BeanFieldDoesNotExistException(this, pField);
    assert getEncapsulatedData() != null;
    //noinspection unchecked
    return (IStatisticData<VALUE>) getEncapsulatedData().getStatisticData().get(pField);
  }

  /**
   * All field tuples marked as identifiers within this bean.
   * Identifiers could be used to find related beans in two containers. (comparable to primary key columns in DB-systems)
   *
   * @return a set of field tuples
   */
  default Set<FieldValueTuple<?>> getIdentifiers()
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
   * This bean as read only version.
   * This will be a new instance, but the data core stays the same.
   *
   * @return this bean as read only version
   */
  default IBean<BEAN> asReadOnly()
  {
    assert getEncapsulatedData() != null;
    //noinspection unchecked
    return ReadOnlyInvocationHandler.createReadOnlyInstance(IBean.class, this);
  }

  /**
   * A stream containing all fields of this bean.
   * Ignores private fields.
   *
   * @return a stream of bean fields
   */
  default Stream<IField<?>> streamFields()
  {
    assert getEncapsulatedData() != null;
    return getEncapsulatedData().streamFields()
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
  default Stream<FieldValueTuple<?>> stream()
  {
    assert getEncapsulatedData() != null;
    return getEncapsulatedData().stream()
        .filter(pFieldTuple -> !pFieldTuple.getField().isPrivate())
        .filter(pFieldTuple -> getFieldActiveSupplier().isOptionalActive(pFieldTuple.getField()));
  }

  /**
   * Sets the default (map based) data source for this bean.
   * The current values of the bean will be retained in the new data source.
   * May be used to decouple a bean instance from a data source that is based on a database connection for example.
   */
  @WriteOperation
  default void useDefaultEncapsulatedDataSource()
  {
    setEncapsulatedDataSource(new MapBasedBeanDataSource(this));
  }

  @Override
  default Set<BeanReference> getDirectReferences()
  {
    assert getEncapsulatedData() != null;
    return getEncapsulatedData().getDirectReferences();
  }
}
