package de.adito.beans.persistence.datastores.sql.util;

import de.adito.beans.core.*;
import de.adito.beans.core.fields.*;
import de.adito.beans.core.references.IHierarchicalField;
import de.adito.beans.persistence.*;
import org.jetbrains.annotations.Nullable;

/**
 * Serialization utility for bean values.
 *
 * @author Simon Danner, 19.02.2018
 */
public class SQLSerializer
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
  public SQLSerializer(BeanDataStore pBeanDataStore)
  {
    beanDataStore = pBeanDataStore;
  }

  /**
   * Converts a bean data value to its serializable format.
   *
   * @param pTuple a tuple of bean field and associated data value
   * @param <TYPE> the data value's type
   * @return the value in its serializable format
   */
  public <TYPE> String toPersistent(FieldTuple<TYPE> pTuple)
  {
    IField<TYPE> field = pTuple.getField();

    if (field instanceof BeanField)
      return _referenceBean((IBean<?>) pTuple.getValue());

    if (field instanceof ContainerField)
      return _referenceContainer((IBeanContainer<?>) pTuple.getValue());

    if (field instanceof ISerializableField)
      return ((ISerializableField<TYPE>) field).toPersistent(pTuple.getValue());

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
  public <TYPE> TYPE fromPersistent(IField<TYPE> pField, String pSerialString)
  {
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
  private String _referenceBean(@Nullable IBean<?> pBean)
  {
    if (pBean == null)
      return null;
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
  private String _referenceContainer(@Nullable IBeanContainer<?> pContainer)
  {
    if (pContainer == null)
      return null;
    return beanDataStore.findContainerId(pContainer) + SEPARATOR + pContainer.getBeanType().getName();
  }

  /**
   * Dereferences a reference string for a persistent bean.
   *
   * @param pBeanType     the type of the bean to dereference
   * @param pSerialString the serial reference string
   * @return the dereferenced bean
   */
  private IBean<?> _dereferenceBean(Class<? extends IBean> pBeanType, @Nullable String pSerialString)
  {
    if (pSerialString == null)
      return null;
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
  private IBeanContainer<?> _dereferenceBeanContainer(@Nullable String pSerialString)
  {
    if (pSerialString == null)
      return null;
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
   * Generates a error message for non convertable values.
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
