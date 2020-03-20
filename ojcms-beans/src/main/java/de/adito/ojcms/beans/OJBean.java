package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.RequiresEncapsulatedAccess;
import de.adito.ojcms.beans.datasource.*;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDuplicateException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.literals.fields.util.FieldValueTuple;
import de.adito.ojcms.beans.util.BeanReflector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The default implementing abstract class of the bean interface.
 * It holds the encapsulated data core, which is the only state of the bean.
 *
 * This class should be extended by any bean type of the application.
 * It may also be extended by another base class, if more base data is necessary.
 *
 * A specific bean of the application defines its fields static to allow access without reflection.
 * Here is an example:
 * "public class SomeBean extends OJBean<SomeBean> {
 * public static final TextField someField = BeanFieldFactory.create(SomeBean.class)"
 * }"
 *
 * Use the private access modifier to use the field only internally.
 *
 * It's important to use the static field factory to create the fields.
 * So all initial data is stored in the field instance automatically.
 *
 * This bean has implementations for {@link #equals(Object)} and {@link #hashCode()}.
 * They include all fields marked as {@link de.adito.ojcms.beans.annotations.Identifier}.
 *
 * @author Simon Danner, 23.08.2016
 * @see BeanFieldFactory
 */
@RequiresEncapsulatedAccess
public abstract class OJBean implements IBean
{
  static final String ENCAPSULATED_DATA_FIELD_NAME = "encapsulatedData";
  private final IEncapsulatedBeanData encapsulatedData;

  //Initial check for the constant value that is holding the encapsulated data field name
  static
  {
    try
    {
      OJBean.class.getDeclaredField(ENCAPSULATED_DATA_FIELD_NAME);
    }
    catch (NoSuchFieldException pE)
    {
      throw new AssertionError("The encapsulated data holder field has been renamed! Check the constant!");
    }
  }

  /**
   * Creates the bean with the default map based data source.
   * The fields will be reflected from the static definitions (see the example above).
   */
  protected OJBean()
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
  protected OJBean(IBeanDataSource pCustomDataSource)
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
   * Checks for duplicate field definitions for this bean type and fires the creation of this bean.
   */
  private void _checkForDuplicateFieldsAndFireCreation()
  {
    _checkForDuplicateFields();
    BeanCreationEvents.fireCreationIfAnnotationPresent(this);
  }

  /**
   * Checks if the bean has duplicate fields, which is not allowed.
   */
  private void _checkForDuplicateFields()
  {
    final Set<IField<?>> checker = new HashSet<>();

    final List<IField<?>> duplicates = streamFields() //
        .filter(pField -> !checker.add(pField)) //
        .collect(Collectors.toList());

    if (!duplicates.isEmpty())
      throw new BeanFieldDuplicateException(duplicates);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{" + stream() //
        .map(Objects::toString) //
        .collect(Collectors.joining(", ")) + "}";
  }

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

    final OJBean other = (OJBean) pOther;
    return identifiers.stream() //
        .allMatch(pIdentifier -> Objects.equals(pIdentifier.getValue(), other.getValue(pIdentifier.getField())));
  }

  @Override
  public int hashCode()
  {
    final Set<FieldValueTuple<?>> identifiers = getIdentifiers();
    return identifiers.isEmpty() ? super.hashCode() : Objects.hash(identifiers.stream() //
        .map(FieldValueTuple::getValue) //
        .toArray(Object[]::new));
  }
}
