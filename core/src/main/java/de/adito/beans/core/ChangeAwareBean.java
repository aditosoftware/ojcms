package de.adito.beans.core;

import de.adito.beans.core.listener.IBeanChangeListener;
import de.adito.beans.core.util.*;
import de.adito.beans.core.util.exceptions.BeanFlattenException;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.*;

/**
 * Wrapper for any bean to redirect changes from an original bean.
 * This class holds the references to the original bean and to a bean, which may be a representation/copy of the original bean.
 * Trough the usage of the bean listener interface the changes can simply be redirected.
 *
 * This wrapper considers possible excluded or flattened representations of the original bean.
 * So some changes can be ignored and others may be carried to deeper levels of the bean's hierarchy.
 *
 * @param <BEAN> the type of the bean that is wrapped by this class
 * @author Simon Danner, 12.06.2017
 */
class ChangeAwareBean<BEAN extends IBean<BEAN>> implements IModifiableBean<BEAN>, IBeanChangeListener
{
  //Keep the reference to the original bean to avoid GC of the real original bean when using multiple change aware beans in a chain.
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final IBean<?> original;
  private final BEAN representation;

  private final boolean isFlat;
  private final IBeanFieldPredicate fieldPredicate;
  private final _ActivePredicate activePredicate = new _ActivePredicate();

  /**
   * Creates the wrapper to redirect changes from the original bean.
   *
   * @param pOriginal       the original bean
   * @param pRepresentation the original bean's representation (may be a copy etc.)
   * @param pFieldPredicate an optional field predicate to exclude some fields from the original bean
   */
  public ChangeAwareBean(IBean<?> pOriginal, BEAN pRepresentation, boolean pIsFlat, @Nullable IBeanFieldPredicate pFieldPredicate)
  {
    assert pRepresentation.getEncapsulated() != null;
    original = pOriginal;
    representation = pRepresentation;
    isFlat = pIsFlat;
    fieldPredicate = pFieldPredicate;
    //noinspection unchecked
    original.listenWeak(this);
  }

  @Override
  public IBeanEncapsulated<BEAN> getEncapsulated()
  {
    return representation.getEncapsulated();
  }

  @Override
  public IBeanFieldActivePredicate<BEAN> getFieldActiveSupplier()
  {
    return activePredicate;
  }

  @Override
  public Stream<IField<?>> streamFields()
  {
    return IModifiableBean.super.streamFields()
        .filter(pField -> _checkFieldPredicate(pField, representation.getValue(pField)));
  }

  @Override
  public Stream<Map.Entry<IField<?>, Object>> stream()
  {
    return IModifiableBean.super.stream()
        .filter(pEntry -> _checkFieldPredicate(pEntry.getKey(), pEntry.getValue()));
  }

  @Override
  public void fieldAdded(IBean pBean, IField pField)
  {
    //noinspection unchecked
    addField(pField, _getIndexOfAddedField(pBean, pField));
  }

  @Override
  public void fieldRemoved(IBean pBean, IField pField, Object pOldValue)
  {
    //noinspection unchecked
    removeField(pField);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void beanChanged(IBean pBean, IField pField, Object pOldValue)
  {
    IBean bean = pBean;
    IField<?> field = pField;
    //Use the data core to check here, because the field may be excluded by the field predicate in this certain moment
    if (isFlat && !getEncapsulated().containsField(field))
    {
      Object changedValue = pBean.getValue(pField);
      //The values must be an instance of the bean interface, because only bean fields can be flattened
      assert pOldValue instanceof IBean;
      assert changedValue instanceof IBean;
      bean = pBean.flatCopy(false);
      field = _findChangedField((IBean<BEAN>) pOldValue, (IBean<BEAN>) changedValue);
    }
    if (_checkFieldPredicate(field, bean.getValue(field)))
    {
      assert bean.hasField(field); //The field must exist now!
      BeanListenerUtil.setValueAndFire((IBean) this, (IField) field, bean.getValue(field));
    }
  }

  /**
   * Checks, if a bean field and its associated value pass the field predicate of this wrapper.
   *
   * @param pField the field to check
   * @param pValue the current value to check
   * @return <tt>true</tt>, if the values pass
   */
  private boolean _checkFieldPredicate(IField<?> pField, Object pValue)
  {
    return fieldPredicate == null || fieldPredicate.test(pField, pValue);
  }

  /**
   * Calculates the index of a newly added bean field.
   * The index may be different to the original because of possibly excluded fields.
   *
   * @param pBean       the original bean, to which the field has been added
   * @param pAddedField the added field
   * @return the index of the field within the representation of the original bean
   */
  private int _getIndexOfAddedField(IBean<?> pBean, IField<?> pAddedField)
  {
    int originalIndex = pBean.getFieldIndex(pAddedField);
    if (fieldPredicate == null)
      return originalIndex;

    return (int) pBean.streamFields()
        .filter(pField -> pBean.getFieldIndex(pField) < originalIndex) //Remove all fields equal to or above the original index
        .filter(pField -> fieldPredicate.test(pField, pBean.getValue(pField))) //Remove all fields that didn't pass the predicate
        .count(); //The amount of the remaining fields has to be the new index then
  }


  /**
   * Finds the field with different values within two beans of the same type.
   *
   * @param pOldBean the old bean instance
   * @param pNewBean the new bean instance
   * @return the bean field that has a different value
   */
  private IField<?> _findChangedField(IBean<BEAN> pOldBean, IBean<BEAN> pNewBean)
  {
    return BeanUtil.compareBeanValues(pOldBean, pNewBean, pOldBean.streamFields().collect(Collectors.toList()))
        .orElseThrow(BeanFlattenException::new);
  }

  /**
   * A special predicate to define which optional fields are active at a certain time.
   * Excludes optional fields that can not pass the field predicate of this wrapper.
   */
  private class _ActivePredicate implements IBeanFieldActivePredicate<BEAN>
  {
    @Override
    public BEAN getBean()
    {
      return representation.getFieldActiveSupplier().getBean();
    }

    @Override
    public boolean isOptionalActive(IField<?> pField)
    {
      //the predicate should be applied positively to be active
      return _checkFieldPredicate(pField, getEncapsulated().getValue(pField)) && IBeanFieldActivePredicate.super.isOptionalActive(pField);
    }
  }
}
