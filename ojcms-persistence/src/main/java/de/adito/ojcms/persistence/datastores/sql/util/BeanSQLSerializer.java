package de.adito.ojcms.persistence.datastores.sql.util;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableField;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.beans.literals.fields.util.*;
import de.adito.ojcms.persistence.*;
import de.adito.ojcms.persistence.util.EPersistenceMode;
import de.adito.ojcms.sqlbuilder.definition.*;
import org.jetbrains.annotations.*;

import java.util.function.Supplier;

/**
 * Serialization utility for bean values.
 *
 * @author Simon Danner, 19.02.2018
 */
public class BeanSQLSerializer extends DefaultValueSerializer
{
  private static final String SEPARATOR = ";";
  private final Supplier<BeanDataStore> beanDataStoreSupplier;

  /**
   * Creates a new serializer.
   *
   * @param pBeanDataStoreSupplier the data store for persistent bean elements
   */
  public BeanSQLSerializer(Supplier<BeanDataStore> pBeanDataStoreSupplier)
  {
    beanDataStoreSupplier = pBeanDataStoreSupplier;
  }

  @Override
  public @Nullable <VALUE> String toSerial(IColumnValueTuple<VALUE> pColumnValueTuple)
  {
    if (pColumnValueTuple instanceof IBeanFieldTupleBased)
      //noinspection unchecked
      return _toPersistent(((IBeanFieldTupleBased<VALUE>) pColumnValueTuple).getFieldValueTuple());
    return super.toSerial(pColumnValueTuple);
  }

  @Override
  public <VALUE> @Nullable VALUE fromSerial(IColumnIdentification<VALUE> pColumnIdentification, String pSerialValue)
  {
    if (pColumnIdentification instanceof IBeanFieldBased)
      //noinspection unchecked
      return _fromPersistent(((IBeanFieldBased<VALUE>) pColumnIdentification).getBeanField(), pSerialValue);
    return super.fromSerial(pColumnIdentification, pSerialValue);
  }

  /**
   * Converts a bean data value to its serializable format.
   *
   * @param pTuple  a tuple of bean field and associated data value
   * @param <VALUE> the data value's type
   * @return the value in its serializable format
   */
  private <VALUE> String _toPersistent(FieldValueTuple<VALUE> pTuple)
  {
    final IField<VALUE> field = pTuple.getField();
    final VALUE value = pTuple.getValue();

    if (value == null)
      return null;

    if (field instanceof BeanField)
      return _referenceBean((IBean<?>) value);

    if (field instanceof ContainerField)
      return _referenceContainer((IBeanContainer<?>) value);

    if (field instanceof ISerializableField)
      return ((ISerializableField<VALUE>) field).toPersistent(value);

    throw new BeanSerializationException(_notSerializableMessage(field, true));
  }

  /**
   * Converts a persistent value back to the bean's data value.
   *
   * @param pField        the bean field the value belongs to
   * @param pSerialString the persistent value
   * @param <VALUE>       the data type
   * @return the converted data value
   */
  private <VALUE> VALUE _fromPersistent(IField<VALUE> pField, String pSerialString)
  {
    if (pSerialString == null)
      return null;

    if (pField instanceof BeanField)
      //noinspection unchecked
      return (VALUE) _dereferenceBean((Class<? extends IBean<?>>) pField.getDataType(), pSerialString);

    if (pField instanceof ContainerField)
      //noinspection unchecked
      return (VALUE) _dereferenceBeanContainer(pSerialString);

    if (pField instanceof ISerializableField)
      return ((ISerializableField<VALUE>) pField).fromPersistent(pSerialString);

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
    //noinspection rawtypes
    final Class<? extends IBean> beanType = pBean.getClass();
    if (!beanType.isAnnotationPresent(Persist.class))
      throw new BeanSerializationException("Bean references within a persistent bean must always refer to another persistent bean!");
    final Persist annotation = beanType.getAnnotation(Persist.class);
    //noinspection unchecked
    return annotation.mode() == EPersistenceMode.SINGLE ? annotation.containerId() :
        String.valueOf(beanDataStoreSupplier.get().getContainerByPersistenceId(annotation.containerId(), beanType).indexOf(pBean));
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
    return beanDataStoreSupplier.get().findContainerId(pContainer) + SEPARATOR + pContainer.getBeanType().getName();
  }

  /**
   * Dereferences a reference string for a persistent bean.
   *
   * @param pBeanType     the type of the bean to dereference
   * @param pSerialString the serial reference string
   * @return the dereferenced bean
   */
  private IBean<?> _dereferenceBean(@SuppressWarnings("rawtypes") Class<? extends IBean> pBeanType, @NotNull String pSerialString)
  {
    assert pBeanType.isAnnotationPresent(Persist.class);
    final Persist annotation = pBeanType.getAnnotation(Persist.class);
    final BeanDataStore dataStore = beanDataStoreSupplier.get();
    //noinspection unchecked
    return annotation.mode() == EPersistenceMode.SINGLE ? dataStore.getBeanByPersistenceId(pSerialString, pBeanType) :
        dataStore.getContainerByPersistenceId(annotation.containerId(), pBeanType).getBean(Integer.parseInt(pSerialString));
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
      //noinspection unchecked,rawtypes
      return beanDataStoreSupplier.get().getContainerByPersistenceId(parts[0], (Class<? extends IBean>) Class.forName(parts[0]));
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
   * @param <VALUE>   the generic data type
   * @return the error message
   */
  private static <VALUE> String _notSerializableMessage(IField<VALUE> pField, boolean pToSerial)
  {
    return "Unable to " + (pToSerial ? "persist" : "read") + " the value of the bean field " + pField.getName() +
        " with type " + pField.getDataType() + "! The field must either be a reference or serializable field!";
  }
}
