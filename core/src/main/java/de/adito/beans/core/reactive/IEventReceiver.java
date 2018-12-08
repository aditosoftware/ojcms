package de.adito.beans.core.reactive;

import io.reactivex.Observer;

/**
 * Receiver for events that are consumed by an internal {@link Observer}.
 *
 * @author Simon Danner, 24.11.2018
 */
public interface IEventReceiver
{
  /**
   * The internal observer to consume an event.
   *
   * @param pEventType the type of the event
   * @param <EVENT>    the actual runtime type of the event
   * @return the internal observer
   */
  <EVENT extends IEvent<?>> Observer<EVENT> getEventObserverFromType(Class<EVENT> pEventType);
}