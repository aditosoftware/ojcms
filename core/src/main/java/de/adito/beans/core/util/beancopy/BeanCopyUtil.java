package de.adito.beans.core.util.beancopy;

import de.adito.beans.core.*;
import de.adito.beans.core.util.exceptions.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * Utility class for copying beans.
 *
 * @author Simon Danner, 12.04.2018
 */
public final class BeanCopyUtil
{
  /**
   * Creates a copy of a bean.
   * This method expects an existing default constructor for this concrete bean type.
   * If the copy should include deep fields, all deep beans are supposed to have default constructors as well.
   * If it is not possible to provide a default constructor, you may use {@link #createCopy(IBean, boolean, Function, CustomFieldCopy[])}
   * to create bean copies. It allows you to define a custom constructor call to create the new instance.
   *
   * @param pOriginal     the original bean to create the copy of
   * @param pDeepCopy     <tt>true</tt>, if the copy of the bean should also include deep values
   * @param pCustomCopies a collection of custom copy mechanisms for specific bean fields
   * @return a copy of the bean
   */
  public static <BEAN extends IBean<BEAN>> BEAN createCopy(BEAN pOriginal, boolean pDeepCopy, CustomFieldCopy<?>... pCustomCopies)
  {
    try
    {
      return _setValues(pOriginal, tryCopyPerDefaultConstructor(pOriginal), pDeepCopy, pCustomCopies);
    }
    catch (UnsupportedOperationException pE)
    {
      throw new BeanCopyException("A default constructor must exist at " + pOriginal.getClass().getName() + "!" +
                                      "Otherwise use the other method to create copies," +
                                      " where you are able to provide a custom constructor call.");
    }
  }

  /**
   * Creates a copy of a bean.
   * This method should be used, if there's no default constructor to create a new instance automatically.
   * Otherwise use {@link #createCopy(IBean, boolean, CustomFieldCopy[])} to create the copy,
   * where you are not supposed to define a custom constructor call.
   * If the copy should be deep, all deep bean values are supposed to have a default constructors.
   *
   * @param pOriginal              the original bean to create the copy of
   * @param pDeepCopy              <tt>true</tt>, if the copy of the bean should also include deep values
   * @param pCustomConstructorCall a custom constructor call defined as function (the input is the existing bean, the function should create the copy)
   * @param pCustomCopies          a collection of custom copy mechanisms for specific bean fields
   * @return a copy of the bean
   */
  public static <BEAN extends IBean<BEAN>> BEAN createCopy(BEAN pOriginal, boolean pDeepCopy, Function<BEAN, BEAN> pCustomConstructorCall,
                                                           CustomFieldCopy<?>... pCustomCopies)
  {
    return _setValues(pOriginal, pCustomConstructorCall.apply(pOriginal), pDeepCopy, pCustomCopies);
  }

  /**
   * Tries to create a new instance of any object by calling the default constructor.
   *
   * @param pOriginal the original object to create the new instance of
   * @param <TYPE>    the generic type of the object
   * @return the copied instance
   * @throws UnsupportedOperationException, if there's no default constructor
   */
  public static <TYPE> TYPE tryCopyPerDefaultConstructor(TYPE pOriginal) throws UnsupportedOperationException
  {
    try
    {
      //noinspection unchecked
      return (TYPE) pOriginal.getClass().newInstance();
    }
    catch (InstantiationException | IllegalAccessException pE)
    {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Sets the values of the original bean in the copied bean instance.
   * If a deep copy is requested, all deep values will be copied.
   *
   * @param pOriginal     the original bean
   * @param pCopy         the copied bean
   * @param pDeep         <tt>true</tt>, if deep values should be copied
   * @param pCustomCopies a collection of custom copy mechanisms for specific bean fields
   * @param <BEAN>        the type of the bean to set the values
   * @return the copy of the bean
   */
  private static <BEAN extends IBean<BEAN>> BEAN _setValues(BEAN pOriginal, BEAN pCopy, boolean pDeep, CustomFieldCopy<?>[] pCustomCopies)
  {
    Map<IField<?>, Function> customCopiesMap = pDeep ? _createCustomCopiesMap(pCustomCopies) : Collections.emptyMap();
    //noinspection unchecked,RedundantCast
    pCopy.streamFields()
        .forEach(pField -> pCopy.setValue((IField) pField, !pDeep ? pOriginal.getValue(pField) :
            _copyFieldValue((IField) pField, pOriginal.getValue(pField), Optional.ofNullable(customCopiesMap.get(pField)), pCustomCopies)));
    return pCopy;
  }

  /**
   * Creates a copy of a certain field value.
   *
   * @param pField               the bean field to copy the value of
   * @param pValue               the value to copy
   * @param pOptionalCopyCreator a optional function to create the copy {@link CustomFieldCopy}
   * @param pCustomCopies        a collection of custom copy mechanisms for specific bean fields
   * @param <TYPE>               the generic type of the field value
   * @return the copied value
   */
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static <TYPE> TYPE _copyFieldValue(IField<TYPE> pField, TYPE pValue, Optional<Function<TYPE, TYPE>> pOptionalCopyCreator,
                                             CustomFieldCopy<?>[] pCustomCopies)
  {
    try
    {
      return pOptionalCopyCreator
          .map(pCreator -> pCreator.apply(pValue))
          .orElse(pField.copyValue(pValue, pCustomCopies));
    }
    catch (BeanCopyUnsupportedException pE)
    {
      throw new BeanCopyException(pE);
    }
  }

  /**
   * Creates a map from the array of {@link CustomFieldCopy}.
   *
   * @param pCustomCopies the custom field copy mechanisms
   * @return the map based on the array
   */
  private static Map<IField<?>, Function> _createCustomCopiesMap(CustomFieldCopy[] pCustomCopies)
  {
    return Stream.of(pCustomCopies)
        .collect(Collectors.toMap(CustomFieldCopy::getField, CustomFieldCopy::getCopyCreator));
  }
}
