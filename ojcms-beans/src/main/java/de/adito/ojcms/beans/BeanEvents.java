package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.NeverNull;
import de.adito.ojcms.beans.annotations.internal.*;
import de.adito.ojcms.beans.exceptions.bean.*;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.fields.IField;
import de.adito.ojcms.beans.reactive.events.*;
import de.adito.ojcms.beans.statistics.IStatisticData;
import de.adito.ojcms.beans.util.BeanReflector;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Utility methods concerning bean events.
 * Events should be created and spread when a value of a bean was changed or a bean was added or removed to/from a container.
 *
 * @author Simon Danner, 31.01.2017
 */
@RequiresEncapsulatedAccess
final class BeanEvents
{
  private BeanEvents()
  {
  }

  /**
   * Propagates a bean change event to their observing sources.
   *
   * @param pEvent the event to propagate
   */
  static void propagate(AbstractChangeEvent<?, ?> pEvent)
  {
    pEvent.publishEventToSource();
  }

  /**
   * Sets the value for a bean field and propagates the changes via some events.
   * This may influence the active state of an optional field or create/remove a reference, which will be adjusted here.
   * This method only uses the encapsulated data core, so any special behaviour of optional fields, etc. won't matter.
   *
   * @param pBean     the bean, where a value has been changed
   * @param pField    the bean field from which the value has been changed
   * @param pNewValue the new value to set
   * @param <BEAN>    the generic bean type
   * @param <VALUE>   the data type of the bean field
   */
  @SuppressWarnings("unchecked")
  static <BEAN extends IBean<BEAN>, VALUE> void setValueAndPropagate(BEAN pBean, IField<VALUE> pField, VALUE pNewValue)
  {
    _checkNullAllowed(pField, pNewValue);
    final IEncapsulatedBeanData encapsulatedData = pBean.getEncapsulatedData();
    assert encapsulatedData != null;
    assert encapsulatedData.containsField(pField);
    final VALUE oldValue = encapsulatedData.getValue(pField);
    if (!Objects.equals(oldValue, pNewValue))
    {
      final IBeanFieldActivePredicate<BEAN> fieldActiveSupplier = pBean.getFieldActivePredicate();
      //Store before active optional fields to detect differences later on
      final List<IField<?>> optionalActiveFields = encapsulatedData.streamFields()
          .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField))
          .collect(Collectors.toList());
      //Set the new value in the data core
      encapsulatedData.setValue(pField, pNewValue);
      //Find newly activated optional fields and fire them as added fields
      encapsulatedData.streamFields()
          .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField))
          .filter(pActiveField -> !optionalActiveFields.remove(pActiveField))
          .forEach(pNewActiveField -> propagate(new BeanFieldAddition<>(pBean, pNewActiveField)));
      //Fire the remaining as removed fields
      optionalActiveFields.stream()
          .map(pBeforeActiveField -> (IField) pBeforeActiveField)
          .forEach(pRemovedField -> propagate(new BeanFieldRemoval<>(pBean, pRemovedField, encapsulatedData.getValue(pRemovedField))));
      //IMPORTANT: The value change itself must be fired after all optional fields have their correct active state
      propagate(new BeanValueChange<>(pBean, pField, oldValue, pNewValue));
      //Finally adjust the references if necessary
      final Class<? extends IField> fieldType = pField.getClass();
      BeanReflector.doIfAnnotationPresent(fieldType, ReferenceField.class, pReferenceField ->
      {
        final Function<Object, Stream<IReferable>> resolver = pReferenceField.resolverType().getResolver();
        //Remove old references based on the old value
        resolver.apply(oldValue)
            .forEach(pReferable -> pReferable.removeReference(pBean, pField));
        //Add the new ones
        resolver.apply(pNewValue)
            .forEach(pReferable -> pReferable.addWeakReference(pBean, pField));
      });
      //Add a statistic entry if necessary
      Optional.ofNullable(encapsulatedData.getStatisticData().get(pField))
          .map(pData -> (IStatisticData<VALUE>) pData)
          .ifPresent(pData -> pData.addEntry(pNewValue));
    }
  }

  /**
   * A data value for a certain bean field is requested.
   * This method takes care of correct access rules and proper values to return.
   *
   * @param pBean               the bean to get the value from
   * @param pField              the bean field to get the value from
   * @param pAllowPrivateAccess <tt>true</tt> if private access is allowed
   * @param <VALUE>             the data type of the field and the value to retrieve
   * @return the value for the bean field
   * @throws BeanFieldDoesNotExistException if the bean field does not exist at the bean
   * @throws BeanIllegalAccessException if access the bean field's data is not allowed
   * @throws NullValueForbiddenException if a null value would have been returned, but the field is marked as {@link NeverNull}
   */
  static <VALUE> VALUE requestValue(IBean<?> pBean, IField<VALUE> pField, boolean pAllowPrivateAccess)
  {
    final IEncapsulatedBeanData encapsulatedData = pBean.getEncapsulatedData();
    assert encapsulatedData != null;
    if (!encapsulatedData.containsField((Objects.requireNonNull(pField))))
      throw new BeanFieldDoesNotExistException(pBean, pField);
    if (!pAllowPrivateAccess && pField.isPrivate())
      throw new BeanIllegalAccessException(pBean, pField);
    final VALUE value = encapsulatedData.getValue(pField);
    //Check if null is allowed
    if (value == null && (pField.hasAnnotation(NeverNull.class) || pField.getClass().isAnnotationPresent(NeverNull.class)))
      throw new NullValueForbiddenException(pField);
    return value;
  }

  /**
   * A bean has been added to a container.
   * Fires an addition event and registers the container references at the bean.
   * May also add a statistic entry.
   *
   * @param pContainer the container to which the bean has been added
   * @param pBean      the added bean
   * @param <BEAN>     the generic type of the bean
   */
  static <BEAN extends IBean<BEAN>> void beanAdded(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    propagate(new BeanContainerAddition<>(pContainer, pBean));
    //Pass the references of the container to the beans as well
    pContainer.getEncapsulatedData().getDirectReferences()
        .forEach(pNode -> pBean.getEncapsulatedData().addWeakReference(pNode.getBean(), pNode.getField()));
    _tryAddStatisticEntry(pContainer);
  }

  /**
   * A bean has been removed from a container.
   * Fires an removal event and registers the container references at the bean.
   * May also add a statistic entry.
   *
   * @param pContainer the container, where the bean has been removed
   * @param pBean      the removed bean
   * @param <BEAN>     the generic type of the bean
   */
  static <BEAN extends IBean<BEAN>> void beanRemoved(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    propagate(new BeanContainerRemoval<>(pContainer, pBean));
    //Remove the references from the bean, which were created through the container
    pBean.getDirectReferences().stream()
        .filter(pNode -> pNode.getBean().getValue(pNode.getField()) == pContainer) //Filter the references to the affected container
        .forEach(pNode -> pBean.getEncapsulatedData().removeReference(pNode.getBean(), pNode.getField()));
    _tryAddStatisticEntry(pContainer);
  }

  /**
   * Removes a bean from a bean container based on a given delete function.
   * An removal event will be propagated.
   *
   * @param pContainer      the container to remove the bean from
   * @param pDeleteFunction the function to perform the removal based on the data core of the container.
   *                        It returns the removed bean or null, if no bean hasn't been removed
   * @param <BEAN>          the type of the beans in the container
   * @return the optionally removed bean
   */
  static <BEAN extends IBean<BEAN>> Optional<BEAN> removeFromContainer(IBeanContainer<BEAN> pContainer,
                                                                       Function<IEncapsulatedBeanContainerData<BEAN>, BEAN> pDeleteFunction)
  {
    final IEncapsulatedBeanContainerData<BEAN> enc = pContainer.getEncapsulatedData();
    assert enc != null;
    final BEAN removedBean = pDeleteFunction.apply(enc);
    if (removedBean != null)
      beanRemoved(pContainer, removedBean);
    return Optional.ofNullable(removedBean);
  }

  /**
   * Removes beans which apply to a given predicate successfully.
   * It's possible to break after one removal, if you know the predicate should apply to one bean only.
   * Events will be propagated.
   *
   * @param pContainer the bean container to remove from
   * @param pPredicate the predicate to determine which beans should be removed
   * @param pBreak     <tt>true</tt> if the iteration should break after one removal
   * @param <BEAN>     the type of the beans in the container
   * @return <tt>true</tt> if at least one bean has been removed
   */
  static <BEAN extends IBean<BEAN>> boolean removeBeanIf(IBeanContainer<BEAN> pContainer, Predicate<BEAN> pPredicate, boolean pBreak)
  {
    assert pContainer.getEncapsulatedData() != null;
    final Iterator<BEAN> it = pContainer.getEncapsulatedData().iterator();
    boolean removed = false;
    while (it.hasNext())
    {
      final BEAN bean = it.next();
      if (pPredicate.test(bean))
      {
        it.remove();
        beanRemoved(pContainer, bean);
        removed = true;
        if (pBreak)
          break;
      }
    }
    return removed;
  }

  /**
   * Checks, if the value to set might be null and if this is allowed for the field to change the value from.
   *
   * @param pField  the field to change the value from
   * @param pValue  the new value
   * @param <VALUE> the data type of the value
   */
  private static <VALUE> void _checkNullAllowed(IField<VALUE> pField, VALUE pValue)
  {
    if (pValue == null && (pField.hasAnnotation(NeverNull.class) || pField.getClass().isAnnotationPresent(NeverNull.class)))
      throw new NullValueForbiddenException(pField);
  }

  /**
   * Tries to add a statistic entry for a bean container.
   * This method should be called, if a bean has been added or removed.
   *
   * @param pContainer the container, for which an entry may be added
   * @param <BEAN>     the type of the beans in the container
   */
  private static <BEAN extends IBean<BEAN>> void _tryAddStatisticEntry(IBeanContainer<BEAN> pContainer)
  {
    pContainer.getStatisticData().ifPresent(pData -> pData.addEntry(pContainer.size()));
  }
}
