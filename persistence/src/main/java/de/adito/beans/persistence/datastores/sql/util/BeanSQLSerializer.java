package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.*;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.persistence.*;
import de.adito.beans.persistence.datastores.sql.builder.definition.*;
import org.jetbrains.annotations.*;

/**
 * Serialization utility for bean values.
 *
 * @author Simon Danner, 19.02.2018
 */
public class BeanSQLSerializer implements IValueSerializer
{
  private static final String REF_FIELD = IHierarchicalField.class.getSimpleName();
  private static final String SERIALIZABLE_FIELD = ISerializableField.class.getSimpleName();
  private static final String SEPARATOR = ";";

  private final BeanDataStore beanDataStore;

  /**
   * Creates a new serializer.
   *
   * @param pBeanDataStore the data store for persistent bean elements
   */
  public BeanSQLSerializer(BeanDataStore pBeanDataStore)
  {
    beanDataStore = pBeanDataStore;
  }

  @Override
  public @Nullable <TYPE> String toSerial(IColumnValueTuple<TYPE> pColumnValueTuple)
  {
    return pColumnValueTuple instanceof BeanColumnValueTuple ?
        _toPersistent(((BeanColumnValueTuple<TYPE>) pColumnValueTuple).getFieldTuple()) :
        IValueSerializer.DEFAULT.toSerial(pColumnValueTuple);
  }

  @Override
  public <TYPE> @Nullable TYPE fromSerial(IColumnIdentification<TYPE> pColumnIdentification, String pSerialValue)
  {
    return pColumnIdentification instanceof BeanColumnIdentification ?
        _fromPersistent(((BeanColumnIdentification<TYPE>) pColumnIdentification).getBeanField(), pSerialValue) :
        IValueSerializer.DEFAULT.fromSerial(pColumnIdentification, pSerialValue);
  }

  /**
   * Converts a bean data value to its serializable format.
   *
   * @param pTuple a tuple of bean field and associated data value
   * @param <TYPE> the data value's type
   * @return the value in its serializable format
   */
  private <TYPE> String _toPersistent(FieldTuple<TYPE> pTuple)
  {
    final IField<TYPE> field = pTuple.getField();
    final TYPE value = pTuple.getValue();

    if (value == null)
      return null;

    if (field instanceof BeanField)
      return _referenceBean((IBean<?>) value);

    if (field instanceof ContainerField)
      return _referenceContainer((IBeanContainer<?>) value);

    if (field instanceof ISerializableField)
      return ((ISerializableField<TYPE>) field).toPersistent(value);

    throw new BeanSerializationException(_notSerializableMessage(field, true));
  }

  /**
   * Converts a persistent value back to the bean's data value.
   *
   * @param pField        the bean field the value belongs to
   * @param pSerialString the persistent value
   * @param <TYPE>        the data type
   * @return the converted data value
   */
  private <TYPE> TYPE _fromPersistent(IField<TYPE> pField, String pSerialString)
  {
    if (pSerialString == null)
      return null;

    if (pField instanceof BeanField)
      //noinspection unchecked
      return (TYPE) _dereferenceBean((Class<? extends IBean>) pField.getType(), pSerialString);

    if (pField instanceof ContainerField)
      //noinspection unchecked
      return (TYPE) _dereferenceBeanContainer(pSerialString);

    if (pField instanceof ISerializableField)
      return ((ISerializableField<TYPE>) pField).fromPersistent(pSerialString);

    throw new BeanSerializationException(_notSerializableMessage(pField, false));
  }

  /**
   * The reference string for a persistent bean.
   * For single beans the container id will be used for the serialization.
   * For container beans the index within the container will be used.
   *
   * @param pBean the bean to create the reference string for
   * @return the reference string
   */
  private String _referenceBean(@NotNull IBean<?> pBean)
  {
    Class<? extends IBean> beanType = pBean.getClass();
    if (!beanType.isAnnotationPresent(Persist.class))
      throw new BeanSerializationException("Bean references within a persistent bean must always refer to another persistent bean!");
    Persist annotation = beanType.getAnnotation(Persist.class);
    //noinspection unchecked
    return annotation.mode() == EPersistenceMode.SINGLE ? annotation.containerId() :
        String.valueOf(beanDataStore.getContainerByPersistenceId(annotation.containerId(), beanType).indexOf(pBean));
  }

  /**
   * The reference string for a persistent bean container.
   * The format for this string is: "PERSISTENCE_ID -> SEPARATOR -> FULLY_QUALIFIED_NAME_BEAN_TYPE".
   *
   * @param pContainer the container to create the reference string for
   * @return the reference string
   */
  private String _referenceContainer(@NotNull IBeanContainer<?> pContainer)
  {
    return beanDataStore.findContainerId(pContainer) + SEPARATOR + pContainer.getBeanType().getName();
  }

  /**
   * Dereferences a reference string for a persistent bean.
   *
   * @param pBeanType     the type of the bean to dereference
   * @param pSerialString the serial reference string
   * @return the dereferenced bean
   */
  private IBean<?> _dereferenceBean(Class<? extends IBean> pBeanType, @NotNull String pSerialString)
  {
    assert pBeanType.isAnnotationPresent(Persist.class);
    Persist annotation = pBeanType.getAnnotation(Persist.class);
    //noinspection unchecked
    return annotation.mode() == EPersistenceMode.SINGLE ? beanDataStore.getBeanByPersistenceId(pSerialString, pBeanType) :
        beanDataStore.getContainerByPersistenceId(annotation.containerId(), pBeanType).getBean(Integer.parseInt(pSerialString));
  }

  /**
   * Dereferences a reference string for a persistent bean container.
   *
   * @param pSerialString the serial reference string
   * @return the dereferenced bean container
   */
  private IBeanContainer<?> _dereferenceBeanContainer(@NotNull String pSerialString)
  {
    String[] parts = pSerialString.split(SEPARATOR);
    if (parts.length != 2)
      throw new BeanSerializationException("Corrupted bean container reference: " + pSerialString);
    try
    {
      //noinspection unchecked
      return beanDataStore.getContainerByPersistenceId(parts[0], (Class<? extends IBean>) Class.forName(parts[0]));
    }
    catch (ClassNotFoundException pE)
    {
      throw new BeanSerializationException(pE);
    }
  }

  /**
   * Generates an error message for non convertable values.
   *
   * @param pField    the bean field the value belongs to
   * @param pToSerial <tt>true</tt>, if the conversion is from data value to persistent value
   * @param <TYPE>    the generic data type
   * @return the error message
   */
  private <TYPE> String _notSerializableMessage(IField<TYPE> pField, boolean pToSerial)
  {
    return "Unable to " + (pToSerial ? "persist" : "read") + " the value of the bean field " + pField.getName() +
        " with type " + pField.getType() + "! The field must either be a " + REF_FIELD + " or a " + SERIALIZABLE_FIELD + "!";
  }
}
