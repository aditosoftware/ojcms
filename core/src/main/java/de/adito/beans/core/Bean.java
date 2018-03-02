package de.adito.beans.core;

import de.adito.beans.core.fields.FieldTuple;
import de.adito.beans.core.util.BeanReflector;
import de.adito.beans.core.util.exceptions.BeanFieldDoesNotExistException;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.*;

/**
 * The default concrete class of the bean interface.
 * It holds the encapsulated data core, which is the only state of the bean.
 *
 * This class should be extended by any bean type of the application.
 * It may also be extended by another base class, if more base data is necessary.
 *
 * It also provides the possibility the read and change private data.
 * This can be used to enable the typical behaviour of any Java POJO.
 *
 * A specific bean of the application defines its fields as static to allow access without reflection.
 * Here is an example:
 * "public class SomeBean extends Bean {
 * public static final TextField someField = BeanFieldFactory.create(SomeBean.class)"
 * }"
 *
 * It's important to use the static field factory to create the fields.
 * So all initial data is automatically stored in the field instance.
 *
 * @param <BEAN> the specific type of this bean, especially if it is used as base class
 * @author Simon Danner, 23.08.2016
 * @see BeanFieldFactory
 */
public class Bean<BEAN extends IBean<BEAN>> implements IBean<BEAN>
{
  private static final Logger LOGGER = Logger.getLogger(Bean.class.getName());
  private IBeanEncapsulated<BEAN> encapsulated;

  /**
   * Creates the bean with a default encapsulated core builder, which is based on a map.
   */
  public Bean()
  {
    _init(new DefaultEncapsulatedBuilder(BeanReflector.getBeanMetadata(getClass())));
  }

  /**
   * Creates the bean with a custom encapsulated core builder, which allows custom implementations for the data source (e.g. database)
   *
   * @param pEncapsulatedBuilder the data core builder
   */
  public Bean(EncapsulatedBuilder.IBeanEncapsulatedBuilder pEncapsulatedBuilder)
  {
    _init(pEncapsulatedBuilder);
  }

  @Override
  public IBeanEncapsulated<BEAN> getEncapsulated()
  {
    return encapsulated;
  }

  /**
   * Returns the value of a private bean field.
   *
   * @param pField the field to which the value should be returned
   * @param <TYPE> the data type of the field
   * @return the field's value
   */
  protected <TYPE> TYPE getPrivateValue(IField<TYPE> pField)
  {
    if (!hasField(pField))
      throw new BeanFieldDoesNotExistException(this, pField);
    _checkNotPrivateAndWarn(pField);
    return encapsulated.getValue(pField);
  }

  /**
   * Sets the value of a private bean field.
   *
   * @param pField the field to which the value should be set
   * @param pValue the new value
   * @param <TYPE> the data type of the field
   */
  protected <TYPE> void setPrivateValue(IField<TYPE> pField, TYPE pValue)
  {
    if (!hasField(pField))
      throw new BeanFieldDoesNotExistException(this, pField);
    _checkNotPrivateAndWarn(pField);
    //noinspection unchecked
    BeanListenerUtil.setValueAndFire((BEAN) this, pField, pValue);
  }

  /**
   * Sets the encapsulated data core for this bean.
   * The data core will be created by {@link EncapsulatedBuilder} based on a {@link de.adito.beans.core.EncapsulatedBuilder.IBeanEncapsulatedBuilder}
   *
   * @param pBuilder the builder to create the data core
   */
  void setEncapsulated(EncapsulatedBuilder.IBeanEncapsulatedBuilder pBuilder)
  {
    //noinspection unchecked
    encapsulated = EncapsulatedBuilder.createBeanEncapsulated(pBuilder, (Class<BEAN>) getClass());
  }

  /**
   * Initializes the data core based on a {@link de.adito.beans.core.EncapsulatedBuilder.IBeanEncapsulatedBuilder}
   *
   * @param pBuilder the builder to construct the data core
   */
  private void _init(EncapsulatedBuilder.IBeanEncapsulatedBuilder pBuilder)
  {
    setEncapsulated(pBuilder);
    BeanCreationRegistry.fireCreationIfAnnotationPresent(this);
  }

  /**
   * Checks, if the field the value should be set or retrieved for, is really private.
   * Otherwise the public methods of {@link IBean} should be used.
   * A misconfiguration will only result in a logger warning.
   *
   * @param pField the field to check
   * @param <TYPE> the generic data type of the field
   */
  private <TYPE> void _checkNotPrivateAndWarn(IField<TYPE> pField)
  {
    if (!pField.isPrivate())
      LOGGER.log(Level.WARNING, "The field '" + pField.getName() + "' is not private. Use the public method to get/set the value instead!");
  }

  /**
   * Default encapsulated data core based on a map to store the bean fields' values.
   */
  public static class DefaultEncapsulatedBuilder implements EncapsulatedBuilder.IBeanEncapsulatedBuilder
  {
    private final Map<IField<?>, Object> values;

    public DefaultEncapsulatedBuilder(List<IField<?>> pFields)
    {
      values = pFields.stream()
          .collect(LinkedHashMap::new, (pMap, pField) -> pMap.put(pField, null), HashMap::putAll);
    }

    public DefaultEncapsulatedBuilder(Map<? extends IField<?>, Object> pPreset)
    {
      values = new LinkedHashMap<>(pPreset);
    }

    @Override
    public <TYPE> TYPE getValue(IField<TYPE> pField)
    {
      //noinspection unchecked
      return (TYPE) values.get(pField);
    }

    @Override
    public <TYPE> void setValue(IField<TYPE> pField, TYPE pValue, boolean pAllowNewField)
    {
      boolean existing = values.containsKey(pField);
      if (!pAllowNewField && !existing)
        throw new RuntimeException("It is not allowed to add new fields for this bean. field: " + pField.getName());
      values.put(pField, pValue);
    }

    @Override
    public <TYPE> void removeField(IField<TYPE> pField)
    {
      values.remove(pField);
    }

    @NotNull
    @Override
    public Iterator<FieldTuple<?>> iterator()
    {
      return new Iterator<FieldTuple<?>>()
      {
        private final Iterator<Map.Entry<IField<?>, Object>> mapIterator = values.entrySet().iterator();

        @Override
        public boolean hasNext()
        {
          return mapIterator.hasNext();
        }

        @Override
        public FieldTuple<?> next()
        {
          Map.Entry<IField<?>, Object> current = mapIterator.next();
          //noinspection unchecked
          return new FieldTuple(current.getKey(), current.getValue());
        }
      };
    }
  }
}
