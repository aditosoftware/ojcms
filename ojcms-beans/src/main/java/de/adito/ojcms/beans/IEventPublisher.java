package de.adito.ojcms.beans;

import de.adito.ojcms.beans.annotations.internal.EncapsulatedData;
import de.adito.ojcms.beans.reactive.IEvent;
import io.reactivex.Observable;

/**
 * Publishes events via internal {@link Observable} instances.
 *
 * @author Simon Danner, 24.11.2018
 */
@EncapsulatedData
interface IEventPublisher
{
  /**
   * Creates an {@link Observable} for a certain event type.
   *
   * @param pEventType the event type
   * @param <EVENT>    the actual runtime event type
   * @return the observable for certain events
   */
  <EVENT extends IEvent<?>> Observable<EVENT> observeByType(Class<EVENT> pEventType);
}