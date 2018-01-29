package de.adito.beans.core;

import de.adito.beans.core.annotations.Identifier;
import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.references.IHierarchicalBeanStructure;
import de.adito.beans.core.statistics.IStatisticData;
import de.adito.beans.core.util.*;
import de.adito.beans.core.util.exceptions.BeanFieldDoesNotExistException;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.*;

/**
 * The functional wrapper interface of a bean.
 * A bean is separated in this wrapper and an encapsulated data core.
 * This interface provides the whole functionality via default methods.
 * The default methods use the only non-default method {@link IEncapsulatedHolder#getEncapsulated()} to get access to the data core.
 * This method may be called 'virtual field', because it gives access to an imaginary field that holds the data core.
 * This means you only have to give a reference to any bean core to get a completed bean, when this interface is used.
 *
 * This interface is implemented by the default bean class {@link Bean}, which is used to create the application's beans.
 * But it may also be used for any other class that should be treated as bean.
 * Furthermore you are able to extend this interface through special methods for your use case.
 * Through the use of an interface it is possible to extend the bean type to a class that already extends another class.
 * This might seem like a solution to the not available multi inheritance in Java, but here only the base interface type
 * is transferred to the extending class. Methods and the static field definitions stay at the concrete bean types.
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
    if (!hasField(pField))
      throw new BeanFieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new UnsupportedOperationException();
    assert getEncapsulated() != null;
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
        .orElseThrow(() -> new RuntimeException("type: " + pConvertType.getSimpleName()))
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
    if (!hasField(pField))
      throw new BeanFieldDoesNotExistException(this, pField);
    if (pField.isPrivate())
      throw new UnsupportedOperationException();
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
              .orElseThrow(() -> new RuntimeException("type: " + sourceType.getSimpleName()))
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
   * An interface to determine if an optional bean field is active at a certain time.
   *
   * @see IBeanFieldActivePredicate
   */
  default IBeanFieldActivePredicate<BEAN> getFieldActiveSupplier()
  {
    //noinspection unchecked
    return () -> (BEAN) this;
  }

  /**
   * Determines if this bean has a certain field.
   *
   * @param pField the bean field to check
   * @return <tt>true</tt> if the field is present
   */
  default boolean hasField(IField pField)
  {
    //Compare reference, because fields are defined as static
    return streamFields().anyMatch(pExistingField -> pField == pExistingField);
  }

  /**
   * The amount of fields of this bean.
   */
  default int getFieldCount()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getFieldCount();
  }

  /**
   * The index of a bean field.
   * Generally the index depends on the order of the defined fields.
   *
   * @param pField the bean field
   * @param <TYPE> the field's data type
   * @return the index of the field
   */
  default <TYPE> int getFieldIndex(IField<TYPE> pField)
  {
    assert getEncapsulated() != null;
    return getEncapsulated().getFieldIndex(pField);
  }

  /**
   * A hierarchical structure of references to this bean.
   * The structure contains direct and deep parent-references to this bean.
   * A reference occurs through a bean or container field. (Default Java references are ignored)
   *
   * @return a interface to retrieve information about the hierarchical reference structure
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
    getEncapsulated().addListener(pListener);
  }

  /**
   * Unregisters a change listener.
   *
   * @param pListener the listener to deregister
   */
  default void unlisten(IBeanChangeListener<BEAN> pListener)
  {
    assert getEncapsulated() != null;
    getEncapsulated().removeListener(pListener);
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
    if (!hasField(pField))
      return null; //TODO exception
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().getStatisticData().get(pField);
  }

  /**
   * All fields marked as identifiers within this bean.
   * Identifiers could be used to find related beans in two containers. (comparable to primary key columns in DB-systems)
   */
  default Collection<IField<?>> getIdentifiers()
  {
    return streamFields()
        .filter(pField -> pField.hasAnnotation(Identifier.class))
        .collect(Collectors.toList());
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
   * Creates a copy of this bean with excluded fields.
   * The copy may get updated, when the original bean has changed.
   *
   * @param pFieldPredicate determines, which fields should stay in the copy
   * @param pUpdateChanges  <tt>true</tt>, if values should be updated in the copy as well
   * @return a reduced copy of the bean
   */
  default IBean reducedCopy(IBeanFieldPredicate pFieldPredicate, boolean pUpdateChanges)
  {
    assert getEncapsulated() != null;
    IBean<?> reducedCopy = new BeanCopy(BeanUtil.asMap(this, pFieldPredicate), getFieldActiveSupplier());
    if (pUpdateChanges)
      reducedCopy = BeanListenerUtil.makeChangeAware(this, reducedCopy, false, pFieldPredicate);
    return reducedCopy;
  }

  /**
   * Creates a flat copy of this bean. (Not deep!)
   * All bean fields will be replaced by the fields of the referred beans.
   * Additionally the copy will be informed via listeners, when a value of the original bean changes.
   * Because of this feature this method is restricted to only one iteration of flattening.
   * Otherwise it may be impossible to determine which flat field belongs to what field in the original bean, due to multiple possibilities.
   *
   * @return a flat copy of this bean (not deep!)
   */
  default IBean flatCopyWithUpdates()
  {
    IBean flatCopy = flatCopy(false);
    return BeanListenerUtil.makeChangeAware(this, flatCopy, true, null);
  }

  /**
   * Creates a flat copy of this bean.
   * All bean fields will be replaced by the fields of the referred beans.
   *
   * @param pDeep <tt>true</tt>, if the fields should be flattened iteratively
   * @return the flat copy of this bean
   */
  default IBean flatCopy(boolean pDeep)
  {
    assert getEncapsulated() != null;
    return new BeanCopy(BeanFlattenUtil.createFlatCopy(this, pDeep), getFieldActiveSupplier());
  }

  /**
   * A stream containing all fields of this bean.
   */
  default Stream<IField<?>> streamFields()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().streamFields()
        .filter(pField -> getFieldActiveSupplier().isOptionalActive(pField));
  }

  /**
   * This bean as stream.
   * It contains key value pairs describing the field-value combinations.
   */
  default Stream<Map.Entry<IField<?>, Object>> stream()
  {
    assert getEncapsulated() != null;
    return getEncapsulated().stream()
        .filter(pEntry -> getFieldActiveSupplier().isOptionalActive(pEntry.getKey()));
  }
}
