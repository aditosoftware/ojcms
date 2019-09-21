package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.beans.exceptions.bean.*;
import de.adito.ojcms.beans.exceptions.field.*;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.*;
import de.adito.ojcms.beans.references.*;
import de.adito.ojcms.beans.statistics.IStatisticData;
import de.adito.ojcms.beans.util.ECopyMode;
import de.adito.ojcms.utils.StringUtility;
import de.adito.ojcms.utils.readonly.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.*;

import static de.adito.ojcms.beans.BeanCopies.doCreateCopy;
import static de.adito.ojcms.beans.BeanInternalEvents.*;
import static java.util.Objects.requireNonNull;

/**
 * The functional wrapper interface of a bean.
 * A bean is separated in this wrapper and an encapsulated data core.
 * This interface provides the whole functionality via default methods.
 * The default methods use the non-default method {@link IEncapsulatedDataHolder#getEncapsulatedData()} to get access to the data core.
 * This method may be called 'virtual field', because it gives access to an imaginary field that holds the data core.
 * This means you only have to give a reference to any bean data core to get a completed bean, if this interface is used.
 *
 * This interface is implemented by the default abstract bean class {@link OJBean}, which is used to create the application's beans.
 * But it may also be used for any other class that should be treated as bean.
 * Furthermore you are able to extend this interface through special methods for your use case.
 *
 * Through the use of an interface it is possible to extend the bean type to a class that already extends another class.
 * This might seem like a solution to the not available multi inheritance in Java, but only the base interface type
 * is transferred to the extending class. Methods and static field definitions stay at the concrete bean type.
 *
 * @param <BEAN> the concrete runtime type of the bean
 * @author Simon Danner, 23.08.2016
 */
@RequiresEncapsulatedAccess
public interface IBean<BEAN extends IBean<BEAN>>
    extends IBeanEventPublisher<FieldValueTuple<?>, BEAN, IBeanDataSource, IEncapsulatedBeanData>, IReferenceProvider
{
  /**
   * The value for a bean field.
   *
   * @param pField  the bean field
   * @param <VALUE> the field's data type
   * @return the value for the bean field
   * @throws BeanFieldDoesNotExistException if the bean field does not exist at the bean
   * @throws NullValueForbiddenException    if a null value would have been returned, but the field is marked as {@link NeverNull}
   */
  default <VALUE> VALUE getValue(IField<VALUE> pField)
  {
    return requestValue(this, pField);
  }

  /**
   * The value for a bean field.
   * If the current value is the initial value of the field, the default value of the field will be returned.
   *
   * @param pField  the bean field
   * @param <VALUE> the field's data type
   * @return the value for the bean field or the field's default value if null
   * @throws BeanFieldDoesNotExistException if the bean field does not exist at the bean
   * @throws NullValueForbiddenException    if a null value would have been returned, but the field is marked as {@link NeverNull}
   */
  default <VALUE> VALUE getValueOrDefault(IField<VALUE> pField)
  {
    final VALUE value = getValue(pField);
    return Objects.equals(value, pField.getInitialValue()) ? pField.getDefaultValue() : value;
  }

  /**
   * The value for a bean field.
   * Here it's possible to define a type to which the value should be transformed before it is returned.
   * The associated field must be able to provide a matching converter.
   *
   * @param pField       the bean field
   * @param pConvertType the type to which the value should be converted
   * @param <VALUE>      the field's data type
   * @param <TARGET>     the generic type to convert to
   * @return the converted value for the bean field
   * @throws BeanFieldDoesNotExistException      if the bean field does not exist at the bean
   * @throws NullValueForbiddenException         if a null value would have been returned, but the field is marked as {@link NeverNull}
   * @throws ValueConversionUnsupportedException if the conversion is not possible
   */
  default <VALUE, TARGET> TARGET getValueConverted(IField<VALUE> pField, Class<TARGET> pConvertType)
  {
    final VALUE actualValue = getValue(pField);
    if (actualValue == null || pConvertType.isAssignableFrom(actualValue.getClass()))
      //noinspection unchecked
      return (TARGET) actualValue;
    return pField.getFromConverter(pConvertType)
        .orElseThrow(() -> new ValueConversionUnsupportedException(pField, pConvertType))
        .apply(actualValue);
  }

  /**
   * Sets a value for a bean field.
   * If the new value is different from the old value, events will be propagated.
   *
   * @param pField  the bean field for which the value should be set
   * @param pValue  the new value
   * @param <VALUE> the field's data type
   * @throws BeanFieldDoesNotExistException if the bean field does not exist at the bean
   * @throws NullValueForbiddenException    if a null value would have been returned, but the field is marked as {@link NeverNull}
   */
  @WriteOperation
  default <VALUE> void setValue(IField<VALUE> pField, VALUE pValue)
  {
    setValueAndPropagate(toRuntimeBean(this), pField, pValue);
  }

  /**
   * Sets a value for a bean field.
   * Here the value may differ from the field's data type.
   * In this case, a converter, provided by the field, will be used to convert the value beforehand.
   * If there is no matching converter, a runtime exception will be thrown.
   * If the new value is different from the old value, events will be propagated.
   *
   * @param pField          the bean field for which the value should be set
   * @param pValueToConvert the new value that possibly has to be transformed beforehand
   * @param <VALUE>         he field's data type
   * @param <SOURCE>        the value's type before its conversion
   * @throws BeanFieldDoesNotExistException      if the bean field does not exist at the bean
   * @throws NullValueForbiddenException         if a null value would have been returned, but the field is marked as {@link NeverNull}
   * @throws ValueConversionUnsupportedException if the conversion is not possible
   */
  @WriteOperation
  @SuppressWarnings("unchecked")
  default <VALUE, SOURCE> void setValueConverted(IField<VALUE> pField, SOURCE pValueToConvert)
  {
    VALUE convertedValue = null;
    if (pValueToConvert != null)
    {
      final Class<SOURCE> sourceType = (Class<SOURCE>) pValueToConvert.getClass();
      convertedValue = requireNonNull(pField).getDataType().isAssignableFrom(sourceType) ? (VALUE) pValueToConvert :
          pField.getToConverter(sourceType)
              .orElseThrow(() -> new ValueConversionUnsupportedException(pField, sourceType))
              .apply(pValueToConvert);
    }
    setValue(pField, convertedValue);
  }

  /**
   * Clears the values of all public field's of this bean back to the initial value of every field.
   * The clear operation ignores fields with a null initial value value and annotated by {@link NeverNull}.
   */
  @WriteOperation
  default void clear()
  {
    //noinspection unchecked
    streamFields()
        .filter(pField -> !pField.isPrivate())
        .filter(pField -> pField.getInitialValue() != null || !pField.hasAnnotation(NeverNull.class))
        .forEach(pField -> setValue((IField) pField, pField.getInitialValue()));
  }

  /**
   * A predicate to determine, if an optional bean field is active at a certain time.
   *
   * @see IBeanFieldActivePredicate
   */
  default IBeanFieldActivePredicate<BEAN> getFieldActivePredicate()
  {
    return () -> toRuntimeBean(this);
  }

  /**
   * Determines, if this bean has a certain field.
   *
   * @param pField the bean field to check
   * @return <tt>true</tt> if the field is present
   */
  default boolean hasField(IField<?> pField)
  {
    return requestEncapsulatedData(this).containsField(pField);
  }

  /**
   * The amount of fields of this bean.
   * Ignores private fields.
   *
   * @return the public field count
   */
  default int getFieldCount()
  {
    return (int) streamFields().count();
  }

  /**
   * The index of a bean field.
   * The index depends on the order of the defined fields generally.
   *
   * @param pField  the bean field
   * @param <VALUE> the field's data type
   * @return the index of the field, or -1 if not present
   */
  default <VALUE> int getFieldIndex(IField<VALUE> pField)
  {
    return streamFields()
        .collect(Collectors.toList())
        .indexOf(pField);
  }

  /**
   * A field of this bean by its name.
   * This method will lead to a runtime exception, if such a field isn't existing.
   *
   * @param pFieldName the name of the field
   * @return the bean field with the name to search for
   * @throws BeanFieldDoesNotExistException if the bean field could not be found
   */
  default IField<?> getFieldByName(String pFieldName)
  {
    StringUtility.requireNotEmpty(pFieldName, "field name");
    return streamFields()
        .filter(pField -> pField.getName().equals(pFieldName))
        .findAny()
        .orElseThrow(() -> new BeanFieldDoesNotExistException(this, pFieldName));
  }

  /**
   * Creates a copy of this bean.
   * This method expects an existing default constructor for this concrete bean type.
   * If the copy should include deep fields, all deep beans are supposed to have default constructors as well.
   * If it is not possible to provide a default constructor, you may use {@link IBean#createCopy(ECopyMode, UnaryOperator, CustomFieldCopy[])}
   * to create bean copies. It allows you to define a custom constructor call to create the new instance.
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
    return doCreateCopy(toRuntimeBean(this), pMode, pCustomFieldCopies);
  }

  /**
   * Creates a copy of this bean.
   * This method should be used, if there's no default constructor to create a new instance automatically.
   * Otherwise use {@link IBean#createCopy(ECopyMode, CustomFieldCopy[])} to create the copy, where you are not supposed to define
   * a custom constructor call. If the copy should be deep, all deep bean values are supposed to have a default constructors.
   *
   * @param pMode                  the copy mode
   * @param pCustomConstructorCall a custom constructor call defined as function (the input is the existing bean, the function should create the copy)
   * @param pCustomFieldCopies     a collection of custom copy mechanisms for specific bean fields
   * @return a copy of this bean
   */
  default BEAN createCopy(ECopyMode pMode, UnaryOperator<BEAN> pCustomConstructorCall, CustomFieldCopy<?>... pCustomFieldCopies)
  {
    return doCreateCopy(toRuntimeBean(this), pMode, pCustomConstructorCall, pCustomFieldCopies);
  }

  /**
   * The statistic data for a certain bean field. May not be present.
   *
   * @param pField  the bean field
   * @param <VALUE> the data type of the field
   * @return optional statistic data
   */
  default <VALUE> Optional<IStatisticData<VALUE>> getStatisticData(IField<VALUE> pField)
  {
    final IStatisticData<?> statisticData = requestEncapsulatedDataForField(this, pField).getStatisticData().get(pField);
    //noinspection unchecked
    return Optional.ofNullable(statisticData)
        .map(pData -> (IStatisticData<VALUE>) pData);
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
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Resolves a deep bean within this bean's children. (references created via {@link de.adito.ojcms.beans.literals.fields.types.BeanField}
   * The bean will be resolved based on a chain of bean fields, which lead the way to the deep bean.
   *
   * @param pChain the chain of bean fields that leads the way to the deep bean value
   * @return the deep bean within this bean's children
   */
  @NotNull
  default IBean<?> resolveDeepBean(IField<?>... pChain)
  {
    return resolveDeepBean(Arrays.asList(pChain));
  }

  /**
   * Resolves a deep bean within this bean's children. (references created via {@link de.adito.ojcms.beans.literals.fields.types.BeanField}
   * The bean will be resolved based on a chain of bean fields, which lead the way to the deep bean.
   *
   * @param pChain the chain of bean fields that leads the way to the deep bean value
   * @return the deep bean within this bean's children
   */
  @NotNull
  default IBean<?> resolveDeepBean(List<IField<?>> pChain)
  {
    IBean<?> current = this;
    for (IField<?> field : requireNonNull(pChain))
    {
      if (!current.hasField(field))
        throw new InvalidChainException(current, field);
      final Object value = current.getValue(field);
      if (!(value instanceof IBean))
        throw new InvalidChainException(field);
      current = (IBean<?>) value;
    }

    return current;
  }

  /**
   * Resolves a deep value within this bean's children. (references created via {@link de.adito.ojcms.beans.literals.fields.types.BeanField}
   * The starting point is this bean, from which a chain of bean fields lead to the certain field to retrieve the deep value from.
   *
   * @param pDeepField the deep field to resolve the value to
   * @param pChain     the chain of bean fields that describes the way to the deep bean
   * @param <VALUE>    the data type of the deep field
   * @return the value of the deep field
   */
  @Nullable
  default <VALUE> VALUE resolveDeepValue(IField<VALUE> pDeepField, IField<?>... pChain)
  {
    return resolveDeepValue(pDeepField, Arrays.asList(pChain));
  }

  /**
   * Resolves a deep value within this bean's children. (references created via {@link de.adito.ojcms.beans.literals.fields.types.BeanField}
   * The starting point is this bean, from which a chain of bean fields lead to the certain field to retrieve the deep value from.
   *
   * @param pDeepField the deep field to resolve the value to
   * @param pChain     the chain of bean fields that describes the way to the deep bean
   * @param <VALUE>    the data type of the deep field
   * @return the value of the deep field
   */
  @Nullable
  default <VALUE> VALUE resolveDeepValue(IField<VALUE> pDeepField, List<IField<?>> pChain)
  {
    final IBean<?> deepBean = resolveDeepBean(pChain);
    if (!deepBean.hasField(pDeepField))
      throw new InvalidChainException(deepBean, pDeepField);
    return deepBean.getValue(pDeepField);
  }

  /**
   * This bean as a read only version.
   * This will be a new instance, but the data core stays the same.
   *
   * @return this bean as a read only version
   */
  default IBean<BEAN> asReadOnly()
  {
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
    return requestEncapsulatedData(this).streamFields()
        .filter(pField -> !pField.isPrivate())
        .filter(pField -> getFieldActivePredicate().isOptionalActive(pField));
  }

  /**
   * This bean as stream. It contains all field value tuples.
   * Ignores private fields.
   *
   * @return a stream of field tuples
   */
  default Stream<FieldValueTuple<?>> stream()
  {
    return requestEncapsulatedData(this).stream()
        .filter(pFieldTuple -> !pFieldTuple.getField().isPrivate())
        .filter(pFieldTuple -> getFieldActivePredicate().isOptionalActive(pFieldTuple.getField()));
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
    return requestEncapsulatedData(this).getDirectReferences();
  }
}
