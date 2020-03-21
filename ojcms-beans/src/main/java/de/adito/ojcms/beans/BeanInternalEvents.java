package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.NeverNull;
import de.adito.ojcms.beans.annotations.internal.*;
import de.adito.ojcms.beans.datasource.IDataSource;
import de.adito.ojcms.beans.exceptions.MissingDataCoreException;
import de.adito.ojcms.beans.exceptions.bean.*;
import de.adito.ojcms.beans.exceptions.field.BeanFieldDoesNotExistException;
import de.adito.ojcms.beans.literals.fields.IField;
import de.adito.ojcms.beans.reactive.events.*;
import de.adito.ojcms.beans.statistics.IStatisticData;
import de.adito.ojcms.beans.util.BeanReflector;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods for internal bean events concerning the encapsulated data cores.
 * One main purpose is to offer common procedures required multiple times in {@link IBean} or {@link IBeanContainer}.
 * The second purpose is to spread {@link de.adito.ojcms.beans.reactive.IEvent} instances if changes happened to the beans.
 *
 * @author Simon Danner, 31.01.2017
 */
@RequiresEncapsulatedAccess
final class BeanInternalEvents
{
  private BeanInternalEvents()
  {
  }

  /**
   * Requests the encapsulated data core from a {@link IEncapsulatedDataHolder}.
   * This method checks if the data core is present.
   *
   * @param pEncapsulatedHolder the holder of the encapsulated data core
   * @param <E>                 the type of the elements within the data core
   * @param <D>                 the type of the data source of the data core
   * @param <ENCAPSULATED>      the runtime type of the data core
   * @return the encapsulated data core
   * @throws MissingDataCoreException if the data core is not present
   */
  static <E, D extends IDataSource, ENCAPSULATED extends IEncapsulatedData<E, D>> ENCAPSULATED requestEncapsulatedData(
      IEncapsulatedDataHolder<E, D, ENCAPSULATED> pEncapsulatedHolder)
  {
    final ENCAPSULATED encapsulatedData = requireNonNull(pEncapsulatedHolder).getEncapsulatedData();

    if (encapsulatedData == null)
      throw new MissingDataCoreException(pEncapsulatedHolder.getClass());

    return encapsulatedData;
  }

  /**
   * Requests the encapsulated data core from a bean with the assumption that a specific field is present at the bean.
   *
   * @param pBean  the bean to get the data core from
   * @param pField the field expected to be present
   * @return the encapsulated data core of the bean
   * @throws BeanFieldDoesNotExistException if the bean field is not present
   */
  static IEncapsulatedBeanData requestEncapsulatedDataForField(IBean pBean, IField<?> pField)
  {
    final IEncapsulatedBeanData encapsulatedBeanData = requestEncapsulatedData(pBean);

    if (!encapsulatedBeanData.containsField(requireNonNull(pField)))
      throw new BeanFieldDoesNotExistException(pBean, pField);

    return encapsulatedBeanData;
  }

  /**
   * A data value for a certain bean field is requested.
   *
   * @param pBean   the bean to get the value from
   * @param pField  the bean field to get the value from
   * @param <VALUE> the data type of the field and the value to retrieve
   * @return the value for the bean field
   * @throws BeanFieldDoesNotExistException if the bean field does not exist at the bean
   * @throws NullValueForbiddenException    if a null value would have been returned, but the field is marked as {@link NeverNull}
   */
  static <VALUE> VALUE requestValue(IBean pBean, IField<VALUE> pField)
  {
    final VALUE value = requestEncapsulatedDataForField(pBean, pField).getValue(pField);

    //Check if null is allowed
    if (value == null && (pField.mustNeverBeNull()))
      throw new NullValueForbiddenException(pField);

    return value;
  }

  /**
   * Sets the value for a bean field and propagates the changes via some events.
   * This may influence the active state of an optional field or create/remove a reference, which will be adjusted here.
   * This method uses the encapsulated data core only, so any special behaviour of optional fields, etc. won't matter.
   *
   * @param pBean     the bean from which a value has been changed
   * @param pField    the bean field from which the value has been changed
   * @param pNewValue the new value to set
   * @param <VALUE>   the data type of the bean field
   * @throws BeanFieldDoesNotExistException if the bean field does not exist at the bean
   * @throws NullValueForbiddenException    if a null value would have been returned, but the field is marked as {@link NeverNull}
   */
  @SuppressWarnings("unchecked")
  static <VALUE> void setValueAndPropagate(IBean pBean, IField<? extends VALUE> pField, VALUE pNewValue)
  {
    if (pNewValue == null && pField.mustNeverBeNull())
      throw new NullValueForbiddenException(pField);

    final IEncapsulatedBeanData encapsulatedData = requestEncapsulatedDataForField(pBean, pField);

    if (pField.isValueFinal() && encapsulatedData.hasFieldValueBeenSet(pField))
      throw new FieldIsFinalException(pField);

    //We have to check the states of the optional fields and then change the value with a following propagation of the change
    final IBeanFieldActivePredicate fieldActiveSupplier = pBean.getFieldActivePredicate();
    //Store before active optional fields to detect differences later on
    final List<IField<?>> optionalActiveFields = encapsulatedData.streamFields() //
        .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField)) //
        .collect(Collectors.toList());

    final VALUE oldValue = encapsulatedData.getValue(pField); //Store old value for later comparison

    //Set the new value even if it has not changed to mark the field's value as 'set once' in the data core
    encapsulatedData.setValue(pField, pNewValue);

    //If the old value is the same as the new one, we have nothing more to do
    if (Objects.equals(oldValue, pNewValue))
      return;

    //Find newly activated optional fields and fire them as added fields
    encapsulatedData.streamFields() //
        .filter(pBeanField -> pBeanField.isOptional() && fieldActiveSupplier.isOptionalActive(pBeanField)) //
        .filter(pActiveField -> !optionalActiveFields.remove(pActiveField)) //
        .forEach(pNewActiveField -> propagateChange(new BeanFieldAddition<>(pBean, pNewActiveField)));

    //Fire the remaining as removed fields
    optionalActiveFields.stream() //
        .map(pBeforeActiveField -> (IField) pBeforeActiveField) //
        .forEach(pRemovedField -> propagateChange(new BeanFieldRemoval<>(pBean, pRemovedField, encapsulatedData.getValue(pRemovedField))));

    //IMPORTANT: The value change itself must be fired after all optional fields have their correct active state
    propagateChange(new BeanValueChange<>(pBean, pField, oldValue, pNewValue));

    //Adjust the references if necessary
    final Class<? extends IField> fieldType = pField.getClass();
    BeanReflector.doIfAnnotationPresent(fieldType, ReferenceField.class, pReferenceField ->
    {
      final Function<Object, Stream<IReferable>> resolver = pReferenceField.resolverType().getResolver();
      //Remove old references based on the old value
      resolver.apply(oldValue).forEach(pReferable -> pReferable.removeReference(pBean, pField));
      //Add the new ones
      resolver.apply(pNewValue).forEach(pReferable -> pReferable.addWeakReference(pBean, pField));
    });

    //Add a statistic entry if necessary
    Optional.ofNullable(encapsulatedData.getStatisticData().get(pField)) //
        .map(pData -> (IStatisticData<VALUE>) pData) //
        .ifPresent(pData -> pData.addEntry(pNewValue));
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
  static <BEAN extends IBean> void beanAdded(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    //Pass the references of the container to the beans as well
    final IEncapsulatedBeanData beanEncapsulated = requestEncapsulatedData(pBean);
    pContainer.getDirectReferences().forEach(pNode -> beanEncapsulated.addWeakReference(pNode.getBean(), pNode.getField()));

    _tryAddStatisticEntry(pContainer);
    propagateChange(new BeanContainerAddition<>(pContainer, pBean));
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
  static <BEAN extends IBean> void beanRemoved(IBeanContainer<BEAN> pContainer, BEAN pBean)
  {
    //Remove the references from the bean, which were created through the container
    final IEncapsulatedBeanData beanEncapsulated = requestEncapsulatedData(pBean);

    pBean.getDirectReferences().stream() //
        .filter(pNode -> pNode.getBean().getValue(pNode.getField()) == pContainer) //Filter the references to the affected container
        .forEach(pNode -> beanEncapsulated.removeReference(pNode.getBean(), pNode.getField()));

    _tryAddStatisticEntry(pContainer);
    propagateChange(new BeanContainerRemoval<>(pContainer, pBean));
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
  static <BEAN extends IBean> Optional<BEAN> removeFromContainer(IBeanContainer<BEAN> pContainer,
                                                                 Function<IEncapsulatedBeanContainerData<BEAN>, BEAN> pDeleteFunction)
  {
    final BEAN removedBean = pDeleteFunction.apply(requestEncapsulatedData(pContainer));

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
  static <BEAN extends IBean> boolean doRemoveBeanIf(IBeanContainer<BEAN> pContainer, Predicate<BEAN> pPredicate, boolean pBreak)
  {
    final Iterator<BEAN> it = requestEncapsulatedData(pContainer).iterator();
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
   * Propagates a bean change event to their observing sources.
   *
   * @param pEvent the event to propagate
   */
  static void propagateChange(AbstractChangeEvent<?, ?> pEvent)
  {
    pEvent.publishEventToSource();
  }

  /**
   * Tries to add a statistic entry for a bean container.
   * This method should be called, if a bean has been added or removed.
   *
   * @param pContainer the container, for which an entry may be added
   * @param <BEAN>     the type of the beans in the container
   */
  private static <BEAN extends IBean> void _tryAddStatisticEntry(IBeanContainer<BEAN> pContainer)
  {
    pContainer.getStatisticData().ifPresent(pData -> pData.addEntry(pContainer.size()));
  }
}
