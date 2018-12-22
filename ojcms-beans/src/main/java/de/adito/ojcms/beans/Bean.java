package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.beans.exceptions.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.fields.util.FieldValueTuple;
import de.adito.ojcms.beans.util.BeanReflector;

import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * The default implementing abstract class of the bean interface.
 * It holds the encapsulated data core, which is the only state of the bean.
 *
 * This class should be extended by any bean type of the application.
 * It may also be extended by another base class, if more base data is necessary.
 *
 * It also provides the possibility to read and change private data via protected methods.
 * This can be used to enable the typical behaviour of any Java POJO.
 *
 * A specific bean of the application defines its fields static to allow access without reflection.
 * Here is an example:
 * "public class SomeBean extends Bean {
 * public static final TextField someField = BeanFieldFactory.create(SomeBean.class)"
 * }"
 *
 * It's important to use the static field factory to create the fields.
 * So all initial data is stored in the field instance automatically.
 *
 * This bean has implementations for {@link #equals(Object)} and {@link #hashCode()}.
 * They include all fields marked as {@link de.adito.ojcms.beans.annotations.Identifier}.
 *
 * @param <BEAN> the runtime type of this bean
 * @author Simon Danner, 23.08.2016
 * @see BeanFieldFactory
 */
@RequiresEncapsulatedAccess
public abstract class Bean<BEAN extends IBean<BEAN>> implements IBean<BEAN>
{
  static final String ENCAPSULATED_DATA_FIELD_NAME = "encapsulatedData";
  private static final Logger LOGGER = Logger.getLogger(Bean.class.getName());
  private final IEncapsulatedBeanData encapsulatedData;

  //Initial check for the constant value that is holding the encapsulated data field name
  static
  {
    try
    {
      Bean.class.getDeclaredField(ENCAPSULATED_DATA_FIELD_NAME);
    }
    catch (NoSuchFieldException pE)
    {
      throw new AssertionError("The encapsulated data holder field has been renamed! Check the constant!");
    }
  }

  /**
   * Creates the bean with the default map based data source.
   * The fields will be reflect from the static definitions (see the example above).
   */
  protected Bean()
  {
    final List<IField<?>> fieldOrder = BeanReflector.reflectBeanFields(getClass());
    encapsulatedData = new EncapsulatedBeanData(new MapBasedBeanDataSource(fieldOrder), fieldOrder);
    _checkForDuplicateFieldsAndFireCreation();
  }

  /**
   * Creates the bean with a custom data source.
   *
   * @param pCustomDataSource the custom data source
   */
  protected Bean(IBeanDataSource pCustomDataSource)
  {
    encapsulatedData = new EncapsulatedBeanData(pCustomDataSource, BeanReflector.reflectBeanFields(getClass()));
    _checkForDuplicateFieldsAndFireCreation();
  }

  @Override
  public IEncapsulatedBeanData getEncapsulatedData()
  {
    return encapsulatedData;
  }

  /**
   * Returns the value of a private bean field.
   *
   * @param pField  the field to which the value should be returned
   * @param <VALUE> the data type of the field
   * @return the field's value
   */
  protected <VALUE> VALUE getPrivateValue(IField<VALUE> pField)
  {
    assert getEncapsulatedData() != null;
    if (!getEncapsulatedData().containsField(pField))
      throw new BeanFieldDoesNotExistException(this, pField);
    _checkNotPrivateAndWarn(pField);
    return encapsulatedData.getValue(pField);
  }

  /**
   * Sets the value of a private bean field.
   *
   * @param pField  the field for which the value should be set
   * @param pValue  the new value
   * @param <VALUE> the data type of the field
   */
  protected <VALUE> void setPrivateValue(IField<VALUE> pField, VALUE pValue)
  {
    assert getEncapsulatedData() != null;
    if (!getEncapsulatedData().containsField(pField))
      throw new BeanFieldDoesNotExistException(this, pField);
    _checkNotPrivateAndWarn(pField);
    //noinspection unchecked
    BeanEvents.setValueAndPropagate((BEAN) this, pField, pValue);
  }

  /**
   * Checks for duplicate field definitions for this bean type and fires the creation of this bean.
   */
  private void _checkForDuplicateFieldsAndFireCreation()
  {
    _checkForDuplicateFields();
    BeanCreationRegistry.fireCreationIfAnnotationPresent(this);
  }

  /**
   * Checks if the bean has duplicate fields, which is not allowed.
   */
  private void _checkForDuplicateFields()
  {
    final Set<IField<?>> checker = new HashSet<>();
    List<IField<?>> duplicates = streamFields()
        .filter(pField -> !checker.add(pField))
        .collect(Collectors.toList());
    if (!duplicates.isEmpty())
      throw new RuntimeException("A bean cannot define a field twice! duplicates: " + duplicates.stream()
          .map(IField::getName)
          .collect(Collectors.joining(", ")));
  }

  /**
   * Checks if the field the value should be set or retrieved for is really private.
   * Otherwise the public methods of {@link IBean} should be used.
   * A misconfiguration will only result in a logger warning.
   *
   * @param pField  the field to check
   * @param <VALUE> the generic data type of the field
   */
  private static <VALUE> void _checkNotPrivateAndWarn(IField<VALUE> pField)
  {
    if (!pField.isPrivate())
      LOGGER.log(Level.WARNING, "The field '" + pField.getName() + "' is not private. Use the public method to get/set the value instead!");
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" + stream()
        .map(Objects::toString)
        .collect(Collectors.joining(", ")) + "}";
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object pOther)
  {
    if (this == pOther)
      return true;
    if (pOther == null || getClass() != pOther.getClass())
      return false;
    final Set<FieldValueTuple<?>> identifiers = getIdentifiers();
    if (identifiers.isEmpty())
      return false;
    final Bean<BEAN> other = (Bean<BEAN>) pOther;
    return identifiers.stream()
        .allMatch(pIdentifier -> Objects.equals(pIdentifier.getValue(), other.getValue(pIdentifier.getField())));
  }

  @Override
  public int hashCode()
  {
    final Set<FieldValueTuple<?>> identifiers = getIdentifiers();
    return identifiers.isEmpty() ? super.hashCode() : Objects.hash(identifiers.stream()
                                                                       .map(FieldValueTuple::getValue)
                                                                       .toArray(Object[]::new));
  }
}
