package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.beans.exceptions.OJInternalException;
import de.adito.ojcms.beans.exceptions.copy.*;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.CustomFieldCopy;
import de.adito.ojcms.beans.util.*;
import de.adito.ojcms.utils.copy.SneakyCopyUtils;
import de.adito.ojcms.utils.copy.exceptions.CopyUnsupportedException;
import org.objenesis.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Utility class for bean copies.
 * A copy can be created from every bean. The API user has to define what {@link ECopyMode} should be used for the copy.
 *
 * @author Simon Danner, 12.04.2018
 */
@RequiresEncapsulatedAccess
final class BeanCopies
{
  private static final Objenesis COPY_CREATOR = new ObjenesisStd();
  private static Field encapsulatedDataField = null;

  private BeanCopies()
  {
  }

  /**
   * Creates a copy of a bean.
   * If you want to use a custom constructor call to create the copy,
   * you may use {@link #doCreateCopy(IBean, ECopyMode, UnaryOperator, CustomFieldCopy[])}.
   *
   * @param pOriginal     the original bean to create the copy of
   * @param pMode         the copy mode
   * @param pCustomCopies a collection of custom copy mechanisms for specific bean fields
   * @return a copy of the bean
   */
  static <BEAN extends IBean<BEAN>> BEAN doCreateCopy(BEAN pOriginal, ECopyMode pMode, CustomFieldCopy<?>... pCustomCopies)
  {
    BeanInternalEvents.requestEncapsulatedData(pOriginal); //Check if the data core is present
    //noinspection unchecked
    final Class<BEAN> beanType = (Class<BEAN>) BeanReflector.requiresDeclaredBeanType(pOriginal.getClass());
    final List<IField<?>> fieldOrder = pOriginal.streamFields().collect(Collectors.toList());
    final BEAN copyInstance = _createBeanPerDefaultConstructorAndSetDataSource(beanType, fieldOrder)
        .orElse(_createBeanSneakyAndInjectEncapsulatedData(beanType, fieldOrder));
    return _setValues(pOriginal, copyInstance, pMode, pCustomCopies);
  }

  /**
   * Creates a copy of a bean.
   * This method should be used, if you want to use a custom constructor call to create the copy.
   * Otherwise use {@link #doCreateCopy(IBean, ECopyMode, CustomFieldCopy[])},
   * where you are not supposed to define a custom constructor call.
   *
   * @param pOriginal              the original bean to create the copy of
   * @param pMode                  the copy mode
   * @param pCustomConstructorCall a custom constructor call defined as function (the input is the existing bean, the function should create the copy)
   * @param pCustomCopies          a collection of custom copy mechanisms for specific bean fields
   * @return a copy of the bean
   */
  static <BEAN extends IBean<BEAN>> BEAN doCreateCopy(BEAN pOriginal, ECopyMode pMode, UnaryOperator<BEAN> pCustomConstructorCall,
                                                      CustomFieldCopy<?>... pCustomCopies)
  {
    BeanInternalEvents.requestEncapsulatedData(pOriginal); //Check if the data core is present
    return _setValues(pOriginal, pCustomConstructorCall.apply(pOriginal), pMode, pCustomCopies);
  }

  /**
   * Tries to create the bean copy per default constructor call.
   *
   * @param pBeanType the bean type to copy
   * @param <BEAN>    the generic bean type
   * @return an optional instance of the copied bean
   */
  private static <BEAN extends IBean<BEAN>> Optional<BEAN> _createBeanPerDefaultConstructorAndSetDataSource(Class<BEAN> pBeanType,
                                                                                                            List<IField<?>> pFieldOrder)
  {
    try
    {
      final BEAN bean = pBeanType.getDeclaredConstructor().newInstance();
      bean.setEncapsulatedDataSource(new MapBasedBeanDataSource(pFieldOrder));
      return Optional.of(bean);
    }
    catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException pE)
    {
      return Optional.empty();
    }
  }

  /**
   * Creates a bean instances with {@link Objenesis}.
   * Also injects the encapsulated data core with a map based data source.
   *
   * @param pBeanType   the type of the bean to create
   * @param pFieldOrder the ordered fields of the bean
   * @param <BEAN>      the generic type of the bean
   * @return the created bean instance
   */
  private static <BEAN extends IBean<BEAN>> BEAN _createBeanSneakyAndInjectEncapsulatedData(Class<BEAN> pBeanType, List<IField<?>> pFieldOrder)
  {
    final BEAN bean = COPY_CREATOR.newInstance(pBeanType);
    final IBeanDataSource dataSource = new MapBasedBeanDataSource(pFieldOrder);
    final EncapsulatedBeanData encapsulatedData = new EncapsulatedBeanData(dataSource, pFieldOrder);
    try
    {
      if (encapsulatedDataField == null)
      {
        encapsulatedDataField = OJBean.class.getDeclaredField(OJBean.ENCAPSULATED_DATA_FIELD_NAME);
        encapsulatedDataField.setAccessible(true);
      }
      encapsulatedDataField.set(bean, encapsulatedData);
      return bean;
    }
    catch (NoSuchFieldException | IllegalAccessException pE)
    {
      throw new OJInternalException("Unable to set encapsulated data core for bean type " + pBeanType.getName(), pE);
    }
  }

  /**
   * Sets the values of the original bean in the copied bean instance.
   * If a deep copy is requested, all deep values will be copied.
   *
   * @param pOriginal     the original bean
   * @param pCopy         the copied bean
   * @param pMode         the copy mode
   * @param pCustomCopies a collection of custom copy mechanisms for specific bean fields
   * @param <BEAN>        the type of the bean to set the values
   * @return the copy of the bean
   */
  private static <BEAN extends IBean<BEAN>> BEAN _setValues(BEAN pOriginal, BEAN pCopy, ECopyMode pMode, CustomFieldCopy<?>[] pCustomCopies)
  {
    final _BeanValueCopyCreator beanValueCopyCreator = new _BeanValueCopyCreator(pMode, pCustomCopies);
    //noinspection unchecked,RedundantCast
    pCopy.streamFields()
        .forEach(pField -> pCopy.setValue((IField) pField, beanValueCopyCreator.copyFieldValue((IField) pField, pOriginal.getValue(pField))));
    //If required set non bean values as well
    if (pMode.shouldCopyAllFields())
    {
      final _NonBeanValueCopyCreator copyCreator = new _NonBeanValueCopyCreator(pMode);
      BeanReflector.reflectDeclaredNonBeanFields(pOriginal.getClass())
          .forEach(pField -> {
            try
            {
              if (!pField.isAccessible())
                pField.setAccessible(true);
              pField.set(pCopy, copyCreator.copyValue(pField, pField.get(pOriginal)));
            }
            catch (IllegalAccessException pE)
            {
              throw new OJInternalException("Unable to set non bean value while copying a bean!", pE);
            }
          });
    }
    return pCopy;
  }

  /**
   * Creates copies of bean values.
   */
  private static class _BeanValueCopyCreator
  {
    private final ECopyMode mode;
    private final CustomFieldCopy<?>[] customCopies;
    private final Map<IField<?>, UnaryOperator> fieldCustomCopyMapping;

    /**
     * Creates a new copy creator based on a {@link ECopyMode} and some optional custom copy mechanisms.
     *
     * @param pMode         the copy mode
     * @param pCustomCopies some optional custom copy mechanisms
     */
    private _BeanValueCopyCreator(ECopyMode pMode, CustomFieldCopy<?>[] pCustomCopies)
    {
      mode = pMode;
      customCopies = pCustomCopies;
      fieldCustomCopyMapping = Arrays.stream(pCustomCopies)
          .collect(Collectors.toMap(CustomFieldCopy::getField, CustomFieldCopy::getCopyCreator));
    }

    /**
     * Creates a copy of a certain field value.
     *
     * @param pField  the bean field to copy the value of
     * @param pValue  the value to copy
     * @param <VALUE> the generic type of the field value
     * @return the copied value
     */
    <VALUE> VALUE copyFieldValue(IField<VALUE> pField, VALUE pValue)
    {
      if (!mode.shouldCopyDeep())
        return pValue;

      try
      {
        //noinspection unchecked
        return (VALUE) Optional.ofNullable(fieldCustomCopyMapping.get(pField))
            .map(pCreator -> pCreator.apply(pValue))
            .orElse(pField.copyValue(pValue, mode, customCopies));
      }
      catch (BeanCopyNotSupportedException pE)
      {
        throw new BeanCopyException(pE);
      }
    }
  }

  /**
   * Creates copies of non bean values.
   */
  private static class _NonBeanValueCopyCreator
  {
    private final ECopyMode mode;

    /**
     * Creates a new copy creator based on a {@link ECopyMode}.
     *
     * @param pMode the copy mode
     */
    private _NonBeanValueCopyCreator(ECopyMode pMode)
    {
      mode = pMode;
    }

    /**
     * Creates a copy of a certain value based on a declared field.
     *
     * @param pDeclaredField the declared field to copy the value of
     * @param pValue         the value to copy
     * @return the copied value
     */
    Object copyValue(Field pDeclaredField, Object pValue)
    {
      if (!mode.shouldCopyDeep())
        return pValue;

      final Class<?> dataType = pDeclaredField.getType();
      final Type genericType = pDeclaredField.getGenericType();
      final Type[] genericTypes = genericType instanceof ParameterizedType ?
          ((ParameterizedType) genericType).getActualTypeArguments() : new Type[0];
      try
      {
        return SneakyCopyUtils.createDeepCopy(pValue, dataType, genericTypes);
      }
      catch (CopyUnsupportedException pE)
      {
        throw new BeanCopyException("Unable to create a deep copy of a non bean field: " + pDeclaredField, pE);
      }
    }
  }
}
