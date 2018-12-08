package de.adito.beans.core;

import de.adito.beans.core.fields.IField;
import de.adito.beans.core.reactive.events.*;
import io.reactivex.Observable;

/**
 * Provides {@link Observable} instances for bean values and fields.
 * A type implementing this interface must hold an {@link IEncapsulated} data core which acts
 * as an {@link de.adito.beans.core.reactive.IEventPublisher}.
 *
 * @param <BEAN>         the type of the observed beans
 * @param <ENCAPSULATED> the type of the encapsulated held by this interface
 * @author Simon Danner, 07.12.2018
 */
interface IObservableBeanValues<BEAN extends IBean<BEAN>, ENCAPSULATED extends IEncapsulated> extends IEncapsulatedHolder<ENCAPSULATED>
{
  /**
   * Creates an {@link Observable} to observe value changes of bean values.
   *
   * @return an observable that publishes {@link BeanValueChange} events
   */
  default Observable<BeanValueChange<BEAN, ?>> observeValues()
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().observeByType(BeanValueChange.class)
        .map(pChange -> (BeanValueChange<BEAN, ?>) pChange);
  }

  /**
   * Creates an {@link Observable} to observe value changes of bean values from a certain field.
   *
   * @return an observable that publishes {@link BeanValueChange} events of the specific field
   */
  default <VALUE> Observable<BeanValueChange<BEAN, VALUE>> observeFieldValue(IField<VALUE> pField)
  {
    //noinspection unchecked
    return observeValues()
        .filter(pChange -> pChange.getField() == pField)
        .map(pChange -> (BeanValueChange<BEAN, VALUE>) pChange);
  }

  /**
   * Creates an {@link Observable} to observe field addition events.
   *
   * @return an observable that publishes {@link BeanFieldAddition} events
   */
  default Observable<BeanFieldAddition<BEAN, ?>> observeFieldAdditions()
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().observeByType(BeanFieldAddition.class)
        .map(pChange -> (BeanFieldAddition<BEAN, ?>) pChange);
  }

  /**
   * Creates an {@link Observable} to observe field removal events.
   *
   * @return an observable that publishes {@link BeanFieldRemoval} events
   */
  default Observable<BeanFieldRemoval<BEAN, ?>> observeFieldRemovals()
  {
    assert getEncapsulated() != null;
    //noinspection unchecked
    return getEncapsulated().observeByType(BeanFieldRemoval.class)
        .map(pChange -> (BeanFieldRemoval<BEAN, ?>) pChange);
  }
}
